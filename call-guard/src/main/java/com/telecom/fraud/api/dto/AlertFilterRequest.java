package com.telecom.fraud.api.dto;

import com.telecom.fraud.model.AlertStatus;
import com.telecom.fraud.model.RiskLevel;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class AlertFilterRequest {
    private String callerMsisdn;
    private RiskLevel riskLevel;
    private AlertStatus status;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateFrom;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateTo;
}
