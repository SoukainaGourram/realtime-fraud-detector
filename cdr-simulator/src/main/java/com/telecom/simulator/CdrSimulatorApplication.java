package com.telecom.simulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Simulateur de trafic d'appels CDR — Projet Telecom
 * Génère un flux continu de CDR publiés sur Kafka.
 * @author Soukaina — Projet Telecom 2026
 */
@SpringBootApplication
@EnableScheduling
public class CdrSimulatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(CdrSimulatorApplication.class, args);
    }
}
