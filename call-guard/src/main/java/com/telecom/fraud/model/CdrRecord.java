package com.telecom.fraud.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Entité représentant un enregistrement CDR persisté pour la consultation d'historique.
 * Couvre à la fois les appels vocaux et les sessions de données.
 */
@Entity
@Table(name = "cdr_records", indexes = {
    @Index(name = "idx_cdr_caller", columnList = "caller_msisdn"),
    @Index(name = "idx_cdr_service_type", columnList = "service_type"),
    @Index(name = "idx_cdr_start_time", columnList = "start_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CdrRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_id", unique = true, nullable = false)
    private String callId;

    @Column(name = "caller_msisdn", nullable = false)
    private String callerMsisdn;

    @Column(name = "callee_msisdn")
    private String calleeMsisdn;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "duration_seconds")
    private int durationSeconds;

    @Column(name = "call_type")
    private String callType; // OUTGOING, INCOMING, INTERNATIONAL, DATA_SESSION

    @Column(name = "service_type", nullable = false)
    private String serviceType; // VOICE, DATA

    @Column(name = "data_volume_mb")
    private double dataVolumeMb; // Volume consommé pour la DATA

    @Column(name = "apn")
    private String apn; // telecom.ma, etc.

    @Column(name = "cell_id")
    private String cellId;

    @Column(name = "cost")
    private double cost;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.serviceType == null) {
            this.serviceType = "VOICE";
        }
    }

    public static CdrRecord fromCdr(Cdr cdr) {
        LocalDateTime start = cdr.getStartTime() != null
                ? LocalDateTime.ofInstant(cdr.getStartTime(), ZoneId.systemDefault())
                : LocalDateTime.now();

        String service = cdr.getServiceType() != null && !cdr.getServiceType().isBlank()
                ? cdr.getServiceType().toUpperCase()
                : (cdr.getDataVolumeMb() > 0 ? "DATA" : "VOICE");

        return CdrRecord.builder()
                .callId(cdr.getCallId() != null ? cdr.getCallId() : java.util.UUID.randomUUID().toString())
                .callerMsisdn(cdr.getCallerMsisdn())
                .calleeMsisdn(cdr.getCalleeMsisdn())
                .startTime(start)
                .durationSeconds(cdr.getDurationSeconds())
                .callType(cdr.getCallType())
                .serviceType(service)
                .dataVolumeMb(cdr.getDataVolumeMb())
                .apn(cdr.getApn())
                .cellId(cdr.getCellId())
                .cost(cdr.getCost())
                .status(cdr.getStatus() != null ? cdr.getStatus() : "COMPLETED")
                .build();
    }
}
