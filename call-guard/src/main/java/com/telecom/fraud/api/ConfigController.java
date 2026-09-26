package com.telecom.fraud.api;

import com.telecom.fraud.config.DetectionRulesConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * API pour la configuration en temps réel du moteur de détection.
 */
@RestController
@RequestMapping("/api/v1/system/config")
@RequiredArgsConstructor
@Slf4j
public class ConfigController {

    private final DetectionRulesConfig config;

    @GetMapping
    public ResponseEntity<DetectionRulesConfig> getConfig() {
        return ResponseEntity.ok(config);
    }

    @PutMapping
    public ResponseEntity<DetectionRulesConfig> updateConfig(@RequestBody DetectionRulesConfig newConfig) {
        // Mise à jour de la configuration en mémoire (pas persisté dans YAML, reset au redémarrage)
        config.setAlertThreshold(newConfig.getAlertThreshold());
        
        DetectionRulesConfig.Rules rules = config.getRules();
        DetectionRulesConfig.Rules newRules = newConfig.getRules();
        
        if (newRules.getHighVolume() != null) {
            rules.getHighVolume().setMaxCalls(newRules.getHighVolume().getMaxCalls());
            rules.getHighVolume().setScoreWeight(newRules.getHighVolume().getScoreWeight());
        }
        
        if (newRules.getShortCallRatio() != null) {
            rules.getShortCallRatio().setMaxRatio(newRules.getShortCallRatio().getMaxRatio());
            rules.getShortCallRatio().setScoreWeight(newRules.getShortCallRatio().getScoreWeight());
        }
        
        log.info("Moteur de détection mis à jour dynamiquement ! Nouveau seuil: {}", config.getAlertThreshold());
        return ResponseEntity.ok(config);
    }
}
