package com.telecom.fraud.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration des paramètres de détection, injectée depuis application.yml.
 */
@Configuration
@ConfigurationProperties(prefix = "detection")
@Data
public class DetectionRulesConfig {
    private Rules rules = new Rules();
    private int alertThreshold;
    
    @Data
    public static class Rules {
        private RuleConfig highVolume = new RuleConfig();
        private RuleConfig highDiversity = new RuleConfig();
        private RuleConfig shortCallRatio = new RuleConfig();
        private RuleConfig continuousActivity = new RuleConfig();
        private RuleConfig highDataVolume = new RuleConfig();
    }
    
    @Data
    public static class RuleConfig {
        private int maxCalls;
        private int windowMinutes;
        private int scoreWeight;
        private int maxDistinctCallees;
        private double maxRatio;
        private int shortDurationSeconds;
        private int windowHours;
        private int maxPauseMinutes;
        private double maxDataMb; // Seuil max de consommation data (Mo)
    }
}
