package com.telecom.fraud.api;

import com.telecom.fraud.api.dto.AlertDTO;
import com.telecom.fraud.api.dto.AlertFilterRequest;
import com.telecom.fraud.kafka.CdrConsumer;
import com.telecom.fraud.model.Alert;
import com.telecom.fraud.model.AlertStatus;
import com.telecom.fraud.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * API REST des alertes de fraude.
 * Tous les endpoints nÃ©cessitent une authentification JWT sauf /system/health.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class AlertController {

    private final AlertService alertService;
    private final CdrConsumer cdrConsumer;

    // â”€â”€â”€ ALERTES â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @GetMapping("/api/v1/alerts")
    public ResponseEntity<Page<AlertDTO>> getAlerts(AlertFilterRequest filter, Pageable pageable) {
        return ResponseEntity.ok(alertService.getAlerts(filter, pageable));
    }

    /** RÃ©trocompatibilitÃ© avec l'ancien chemin /api/alerts */
    @GetMapping("/api/alerts")
    public ResponseEntity<Page<AlertDTO>> getAlertsLegacy(AlertFilterRequest filter, Pageable pageable) {
        return ResponseEntity.ok(alertService.getAlerts(filter, pageable));
    }

    @GetMapping("/api/v1/alerts/{id}")
    public ResponseEntity<AlertDTO> getAlertById(@PathVariable Long id) {
        Alert alert = alertService.getAlertById(id);
        return alert == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(AlertDTO.fromAlert(alert));
    }

    // â”€â”€â”€ STATISTIQUES â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @GetMapping("/api/v1/alerts/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(alertService.getStats());
    }

    /** RÃ©trocompatibilitÃ© */
    @GetMapping("/api/alerts/stats")
    public ResponseEntity<Map<String, Object>> getStatsLegacy() {
        return ResponseEntity.ok(alertService.getStats());
    }

    /**
     * Statistiques avancÃ©es pour l'admin :
     * - Compteurs par statut (OPEN, INVESTIGATING, RESOLVED, FALSE_POSITIVE)
     * - Compteurs par type de fraude
     * - Top 10 MSISDNs suspects
     * - Taux de rÃ©solution
     */
    @GetMapping("/api/v1/alerts/stats/advanced")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdvancedStats() {
        return ResponseEntity.ok(alertService.getAdvancedStats());
    }

    // â”€â”€â”€ REVIEW (ADMIN / ANALYST) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @PutMapping("/api/v1/alerts/{id}/review")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<AlertDTO> reviewAlert(
            @PathVariable Long id,
            @RequestParam AlertStatus newStatus,
            Authentication authentication) {
        try {
            Alert updated = alertService.reviewAlert(id, authentication.getName(), newStatus);
            return ResponseEntity.ok(AlertDTO.fromAlert(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // â”€â”€â”€ EXPORT CSV â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Exporte les alertes en CSV.
     * ParamÃ¨tre optionnel ?hours=24 (dÃ©faut 24h, max 720h = 30 jours).
     * RÃ©servÃ© aux rÃ´les ANALYST et ADMIN.
     */
    @GetMapping("/api/v1/alerts/export/csv")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<byte[]> exportCsv(@RequestParam(defaultValue = "24") int hours) {
        int safeHours = Math.min(Math.max(hours, 1), 720); // Entre 1h et 30 jours
        String csv = alertService.exportCsv(safeHours);

        String filename = "callguard_alertes_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(csv.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    // â”€â”€â”€ SANTÃ‰ SYSTÃˆME (PUBLIC) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Endpoint de santÃ© systÃ¨me accessible sans authentification.
     * Expose : statut JVM, compteurs Kafka, timestamp.
     * Les checks Kafka/PostgreSQL sont dans /actuator/health (Spring Boot Actuator).
     */
    @GetMapping("/api/v1/system/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        Map<String, Object> health = new LinkedHashMap<>();

        // Informations JVM
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        long usedMb   = memBean.getHeapMemoryUsage().getUsed()  / (1024 * 1024);
        long maxMb    = memBean.getHeapMemoryUsage().getMax()   / (1024 * 1024);
        long committedMb = memBean.getHeapMemoryUsage().getCommitted() / (1024 * 1024);

        Map<String, Object> jvm = new LinkedHashMap<>();
        jvm.put("heapUsedMb",      usedMb);
        jvm.put("heapMaxMb",       maxMb);
        jvm.put("heapCommittedMb", committedMb);
        jvm.put("heapUsagePercent", maxMb > 0 ? Math.round((double) usedMb / maxMb * 100) : 0);
        health.put("jvm", jvm);

        // Statistiques Kafka Consumer
        Map<String, Object> kafka = new LinkedHashMap<>();
        kafka.put("cdrProcessed", cdrConsumer.getProcessedCount());
        kafka.put("dlqMessages",  cdrConsumer.getDlqCount());
        kafka.put("status", "UP");
        health.put("kafka", kafka);

        // Timestamp
        health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        health.put("status", "UP");

        return ResponseEntity.ok(health);
    }
}


