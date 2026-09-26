package com.telecom.fraud.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entité représentant un utilisateur du système.
 */
@Entity
@Table(name = "users")
@Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class User {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password; // hashé BCrypt
    
    @Column(nullable = false)
    private String role; // ROLE_ADMIN, ROLE_ANALYST, ROLE_VIEWER
    
    private boolean enabled;
}
