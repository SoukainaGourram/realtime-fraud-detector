package com.telecom.simulator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Événement CDR.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CdrEvent {
    
    @JsonProperty("call_id")
    private String callId;

    @JsonProperty("caller_msisdn")
    private String callerMsisdn;

    @JsonProperty("callee_msisdn")
    private String calleeMsisdn;

    @JsonProperty("start_time")
    private String startTime;

    @JsonProperty("duration_seconds")
    private int durationSeconds;

    @JsonProperty("call_type")
    private String callType;

    @JsonProperty("cell_id")
    private String cellId;

    @JsonProperty("cost")
    private double cost;

    @JsonProperty("status")
    private String status;

    @JsonProperty("service_type")
    private String serviceType; // VOICE, DATA

    @JsonProperty("data_volume_mb")
    private double dataVolumeMb;

    @JsonProperty("apn")
    private String apn;

    @JsonIgnore
    private String patternType;
}
