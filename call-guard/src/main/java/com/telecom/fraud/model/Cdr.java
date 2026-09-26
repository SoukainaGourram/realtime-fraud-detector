package com.telecom.fraud.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;

/**
 * Objet de transfert pour les enregistrements détaillés d'appels (Call Detail Record).
 * Reçu via Kafka.
 */
@Data
public class Cdr {
    
    @JsonProperty("call_id")
    private String callId;
    
    @JsonProperty("caller_msisdn")
    private String callerMsisdn;
    
    @JsonProperty("callee_msisdn")
    private String calleeMsisdn;
    
    @JsonProperty("start_time")
    private Instant startTime;
    
    @JsonProperty("duration_seconds")
    private int durationSeconds;
    
    @JsonProperty("call_type")
    private String callType; // OUTGOING, INCOMING, INTERNATIONAL
    
    @JsonProperty("cell_id")
    private String cellId;
    
    @JsonProperty("cost")
    private double cost;
    
    @JsonProperty("status")
    private String status; // COMPLETED, FAILED, BUSY

    @JsonProperty("service_type")
    private String serviceType; // VOICE, DATA

    @JsonProperty("data_volume_mb")
    private double dataVolumeMb; // Volume consommé en Mo pour les sessions DATA

    @JsonProperty("apn")
    private String apn; // Access Point Name (ex: telecom.ma, telecom.internet)
}
