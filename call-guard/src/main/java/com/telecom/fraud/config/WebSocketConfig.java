package com.telecom.fraud.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration WebSocket pour la diffusion des alertes en temps réel.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Préfixe pour s'abonner aux alertes (ex: côté client on s'abonne à /topic/alerts)
        config.enableSimpleBroker("/topic");
        // Préfixe pour l'envoi de messages depuis le client (optionnel ici)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Point d'entrée WebSocket, avec support SockJS en fallback
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Permettre CORS pour le dashboard
                .withSockJS();
    }
}
