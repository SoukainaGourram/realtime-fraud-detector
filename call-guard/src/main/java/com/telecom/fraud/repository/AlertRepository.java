package com.telecom.fraud.repository;

import com.telecom.fraud.model.Alert;
import com.telecom.fraud.model.AlertStatus;
import com.telecom.fraud.model.AlertType;
import com.telecom.fraud.model.RiskLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    Page<Alert> findByCallerMsisdn(String msisdn, Pageable pageable);
    Page<Alert> findByRiskLevel(RiskLevel level, Pageable pageable);
    Page<Alert> findByStatus(AlertStatus status, Pageable pageable);
    Page<Alert> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    long countByRiskLevel(RiskLevel level);
    long countByStatus(AlertStatus status);
    long countByAlertType(AlertType type);

    List<Alert> findTop10ByOrderByRiskScoreDesc();
    List<Alert> findAllByOrderByCreatedAtDesc();

    // Anti-doublon : alerte récente pour ce MSISDN (30 minutes)
    List<Alert> findByCallerMsisdnAndCreatedAtAfter(String msisdn, LocalDateTime since);

    // Top MSISDNs les plus suspects (nombre d'alertes par MSISDN, tri décroissant)
    @Query("SELECT a.callerMsisdn, COUNT(a), MAX(a.riskScore) FROM Alert a " +
           "GROUP BY a.callerMsisdn ORDER BY COUNT(a) DESC")
    List<Object[]> findTopSuspectMsisdns(Pageable pageable);

    // Nombre d'alertes par heure (pour le graphe de tendance)
    @Query("SELECT FUNCTION('DATE_TRUNC', 'hour', a.createdAt), COUNT(a) FROM Alert a " +
           "WHERE a.createdAt >= :since GROUP BY FUNCTION('DATE_TRUNC', 'hour', a.createdAt) " +
           "ORDER BY FUNCTION('DATE_TRUNC', 'hour', a.createdAt)")
    List<Object[]> countByHourSince(LocalDateTime since);

    // Toutes les alertes pour l'export CSV (sans pagination)
    List<Alert> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime since);
}
