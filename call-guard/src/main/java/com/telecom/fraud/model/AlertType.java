package com.telecom.fraud.model;

/**
 * Type d'alerte de fraude détectée.
 */
public enum AlertType {
    SIMBOX,
    HIGH_VOLUME,
    HIGH_DIVERSITY,
    SHORT_CALL_RATIO,
    CONTINUOUS_ACTIVITY,
    CLONED_SIM,
    HIGH_DATA_VOLUME,
    BLACKLIST_VIOLATION
}
