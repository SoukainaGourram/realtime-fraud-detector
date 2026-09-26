package com.telecom.fraud.service;

import com.telecom.fraud.api.dto.AlertDTO;
import com.telecom.fraud.api.dto.AlertFilterRequest;
import com.telecom.fraud.model.Alert;
import com.telecom.fraud.model.AlertStatus;
import com.telecom.fraud.model.AlertType;
import com.telecom.fraud.model.RiskLevel;
import com.telecom.fraud.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service de gestion des alertes de fraude.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final AlertRepository alertRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * CrÃ©e une nouvelle alerte et la diffuse via WebSocket.
     * Anti-doublon renforcÃ© : 30 minutes (au lieu de 5).
     */
    @Transactional
    public void createAlert(Alert alert) {
        // Anti-doublon : ignore si une alerte existe dÃ©jÃ  pour ce MSISDN dans la derniÃ¨re minute (idÃ©al pour Projet)
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        List<Alert> recentAlerts = alertRepository.findByCallerMsisdnAndCreatedAtAfter(
                alert.getCallerMsisdn(), oneMinuteAgo);

        if (!recentAlerts.isEmpty()) {
            log.debug("Alerte ignorÃ©e (doublon < 1 min) pour MSISDN: {}", alert.getCallerMsisdn());
            return;
        }

        Alert savedAlert = alertRepository.save(alert);

        // Diffusion WebSocket vers le dashboard
        try {
            messagingTemplate.convertAndSend("/topic/alerts", savedAlert);
        } catch (Exception e) {
            log.error("Erreur WebSocket: {}", e.getMessage());
        }
        log.info("âœ… Nouvelle alerte persistÃ©e â€” MSISDN: {} | Niveau: {} | Score: {}",
                alert.getCallerMsisdn(), alert.getRiskLevel(), alert.getRiskScore());
    }

    /**
     * RÃ©cupÃ¨re les alertes avec filtres optionnels et pagination.
     */
    public Page<AlertDTO> getAlerts(AlertFilterRequest filter, Pageable pageable) {
        Page<Alert> alerts;

        if (filter.getCallerMsisdn() != null && !filter.getCallerMsisdn().isBlank()) {
            alerts = alertRepository.findByCallerMsisdn(filter.getCallerMsisdn(), pageable);
        } else if (filter.getRiskLevel() != null) {
            alerts = alertRepository.findByRiskLevel(filter.getRiskLevel(), pageable);
        } else if (filter.getStatus() != null) {
            alerts = alertRepository.findByStatus(filter.getStatus(), pageable);
        } else {
            alerts = alertRepository.findAll(pageable);
        }

        return alerts.map(AlertDTO::fromAlert);
    }

    public Alert getAlertById(Long id) {
        return alertRepository.findById(id).orElse(null);
    }

    @Transactional
    public Alert reviewAlert(Long id, String reviewedBy, AlertStatus newStatus) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alerte introuvable: " + id));

        alert.setStatus(newStatus);
        alert.setReviewedBy(reviewedBy);
        alert.setReviewedAt(LocalDateTime.now());

        return alertRepository.save(alert);
    }

    /**
     * Statistiques de base pour le dashboard.
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        Map<String, Long> countByLevel = new LinkedHashMap<>();
        for (RiskLevel level : RiskLevel.values()) {
            countByLevel.put(level.name(), alertRepository.countByRiskLevel(level));
        }
        stats.put("countByLevel", countByLevel);

        List<Alert> topAlerts = alertRepository.findTop10ByOrderByRiskScoreDesc();
        stats.put("topAlerts", topAlerts);

        return stats;
    }

    /**
     * Statistiques avancÃ©es pour l'admin : compteurs par statut, par type, top suspects.
     */
    public Map<String, Object> getAdvancedStats() {
        Map<String, Object> stats = new HashMap<>();

        // Par niveau de risque
        Map<String, Long> byLevel = new LinkedHashMap<>();
        for (RiskLevel level : RiskLevel.values()) {
            byLevel.put(level.name(), alertRepository.countByRiskLevel(level));
        }
        stats.put("countByLevel", byLevel);

        // Par statut
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (AlertStatus status : AlertStatus.values()) {
            byStatus.put(status.name(), alertRepository.countByStatus(status));
        }
        stats.put("countByStatus", byStatus);

        // Par type de fraude
        Map<String, Long> byType = new LinkedHashMap<>();
        for (AlertType type : AlertType.values()) {
            byType.put(type.name(), alertRepository.countByAlertType(type));
        }
        stats.put("countByType", byType);

        // Total alertes
        long total = alertRepository.count();
        stats.put("totalAlerts", total);

        // Top 10 MSISDNs suspects
        List<Map<String, Object>> topSuspects = new ArrayList<>();
        List<Object[]> rows = alertRepository.findTopSuspectMsisdns(PageRequest.of(0, 10));
        for (Object[] row : rows) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("msisdn", row[0]);
            entry.put("alertCount", row[1]);
            entry.put("maxScore", row[2]);
            topSuspects.add(entry);
        }
        stats.put("topSuspects", topSuspects);

        // Taux de rÃ©solution
        long resolved = alertRepository.countByStatus(AlertStatus.RESOLVED);
        long falsPositive = alertRepository.countByStatus(AlertStatus.FALSE_POSITIVE);
        stats.put("resolutionRate", total > 0 ? Math.round(((double)(resolved + falsPositive) / total) * 100) : 0);

        return stats;
    }

    /**
     * GÃ©nÃ¨re un CSV de toutes les alertes des derniÃ¨res X heures.
     * @param hours nombre d'heures en arriÃ¨re (dÃ©faut 24)
     */
    public String exportCsv(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        List<Alert> alerts = alertRepository.findByCreatedAtAfterOrderByCreatedAtDesc(since);

        StringBuilder csv = new StringBuilder();
        csv.append("ID,MSISDN,Type,Niveau,Score,Statut,RÃ¨gles dÃ©clenchÃ©es,Date crÃ©ation,RÃ©visÃ© par\n");

        for (Alert a : alerts) {
            csv.append(safe(a.getId())).append(",");
            csv.append(safe(a.getCallerMsisdn())).append(",");
            csv.append(safe(a.getAlertType())).append(",");
            csv.append(safe(a.getRiskLevel())).append(",");
            csv.append(a.getRiskScore()).append(",");
            csv.append(safe(a.getStatus())).append(",");
            // Nettoyer les guillemets dans la description
            String rules = a.getTriggeredRules() != null
                    ? "\"" + a.getTriggeredRules().replace("\"", "'") + "\""
                    : "";
            csv.append(rules).append(",");
            csv.append(safe(a.getCreatedAt())).append(",");
            csv.append(safe(a.getReviewedBy())).append("\n");
        }

        return csv.toString();
    }

    private String safe(Object o) {
        return o == null ? "" : o.toString();
    }
}

