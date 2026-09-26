package com.telecom.fraud.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.telecom.fraud.model.Alert;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AlertDTO {
    private Long id;
    private String alertId;
    private String callerMsisdn;
    private String alertType;
    private int riskScore;
    private String riskLevel;
    private String triggeredRules;
    private String description;
    private String status;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime reviewedAt;
    
    private String reviewedBy;

    public static AlertDTO fromAlert(Alert alert) {
        if (alert == null) {
            return null;
        }
        return AlertDTO.builder()
                .id(alert.getId())
                .alertId(alert.getAlertId())
                .callerMsisdn(alert.getCallerMsisdn())
                .alertType(alert.getAlertType() != null ? alert.getAlertType().name() : null)
                .riskScore(alert.getRiskScore())
                .riskLevel(alert.getRiskLevel() != null ? alert.getRiskLevel().name() : null)
                .triggeredRules(alert.getTriggeredRules())
                .description(alert.getDescription())
                .status(alert.getStatus() != null ? alert.getStatus().name() : null)
                .createdAt(alert.getCreatedAt())
                .reviewedAt(alert.getReviewedAt())
                .reviewedBy(alert.getReviewedBy())
                .build();
    }
}
