package com.telecom.fraud.model;

/**
 * Niveau de risque d'une alerte.
 */
public enum RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    /**
     * Détermine le niveau de risque en fonction du score.
     * @param score Le score de risque (0-100)
     * @return Le niveau de risque correspondant
     */
    public static RiskLevel fromScore(int score) {
        if (score <= 25) return LOW;
        if (score <= 50) return MEDIUM;
        if (score <= 75) return HIGH;
        return CRITICAL;
    }
}
