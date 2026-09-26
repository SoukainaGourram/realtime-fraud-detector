package com.telecom.fraud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Classe principale de l'application de détection de fraude télécom.
 * 
 * Architecture:
 * - Consumer Kafka: reçoit les CDR (Call Detail Records) en temps réel
 * - Moteur de détection: applique des règles sur des fenêtres glissantes
 * - API REST sécurisée: expose les alertes via JWT/RBAC
 * - WebSocket: diffuse les alertes en temps réel au dashboard
 *
 * @author Soukaina — Projet Telecom 2026
 */
@SpringBootApplication
@EnableScheduling
public class FraudDetectionApplication {
    public static void main(String[] args) {
        SpringApplication.run(FraudDetectionApplication.class, args);
    }

    @org.springframework.context.annotation.Bean
    public org.springframework.boot.CommandLineRunner initUsers(
            com.telecom.fraud.repository.UserRepository userRepository,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        return args -> {
            String defaultPassword = passwordEncoder.encode("admin123");
            
            userRepository.findByUsername("admin").ifPresentOrElse(
                u -> { u.setPassword(defaultPassword); userRepository.save(u); },
                () -> userRepository.save(com.telecom.fraud.model.User.builder()
                        .username("admin").password(defaultPassword).role("ROLE_ADMIN").enabled(true).build())
            );

            userRepository.findByUsername("analyst").ifPresentOrElse(
                u -> { u.setPassword(defaultPassword); userRepository.save(u); },
                () -> userRepository.save(com.telecom.fraud.model.User.builder()
                        .username("analyst").password(defaultPassword).role("ROLE_ANALYST").enabled(true).build())
            );

            userRepository.findByUsername("viewer").ifPresentOrElse(
                u -> { u.setPassword(defaultPassword); userRepository.save(u); },
                () -> userRepository.save(com.telecom.fraud.model.User.builder()
                        .username("viewer").password(defaultPassword).role("ROLE_VIEWER").enabled(true).build())
            );
        };
    }
}
