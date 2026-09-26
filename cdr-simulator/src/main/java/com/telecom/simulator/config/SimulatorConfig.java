package com.telecom.simulator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration des paramètres du simulateur.
 */
@Configuration
@ConfigurationProperties(prefix = "simulator")
@Data
public class SimulatorConfig {
    private String kafkaTopic;
    private int ratePerSecond;
    private double fraudRatio;
    private int numNormalLines;
    private int numFraudLines;
}
