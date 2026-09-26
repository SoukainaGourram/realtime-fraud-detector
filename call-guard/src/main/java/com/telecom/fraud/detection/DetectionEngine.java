package com.telecom.fraud.detection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telecom.fraud.config.DetectionRulesConfig;
import com.telecom.fraud.detection.rules.DetectionRule;
import com.telecom.fraud.model.Alert;
import com.telecom.fraud.model.AlertStatus;
import com.telecom.fraud.model.AlertType;
import com.telecom.fraud.model.Cdr;
import com.telecom.fraud.model.RiskLevel;
import com.telecom.fraud.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Moteur de dÃ©tection de fraude.
 * 
 * Orchestrateur principal: pour chaque CDR reÃ§u, applique toutes les
 * rÃ¨gles de dÃ©tection sur la fenÃªtre glissante du MSISDN concernÃ©.
 * Si le score cumulÃ© dÃ©passe le seuil, gÃ©nÃ¨re une alerte.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DetectionEngine {

    private final List<DetectionRule> rules;
    private final SlidingWindowManager windowManager;
    private final DetectionRulesConfig config;
    private final AlertService alertService;
    private final ObjectMapper objectMapper;
    private final com.telecom.fraud.service.BlacklistService blacklistService;

    /**
     * Analyse un CDR entrant et gÃ©nÃ¨re une alerte si fraude dÃ©tectÃ©e.
     */
    public void analyze(Cdr cdr) {
        String msisdn = cdr.getCallerMsisdn();
        if (blacklistService.isBlacklisted(msisdn)) {
            Alert alert = Alert.builder().alertId(UUID.randomUUID().toString()).callerMsisdn(msisdn).alertType(AlertType.BLACKLIST_VIOLATION).riskScore(100).riskLevel(RiskLevel.CRITICAL).triggeredRules("[\"BLACKLIST\"]").description("Tentative d'appel par un numero blacklist").status(AlertStatus.NEW).build();
            alertService.createAlert(alert);
            return;
        }
        
        // 1. Ajouter le CDR dans la fenÃªtre glissante
        windowManager.addCdr(cdr);
        
        // On prend la fenÃªtre la plus large pour l'Ã©valuation globale (ex: 120 minutes)
        int maxWindowMinutes = Math.max(
                config.getRules().getHighVolume().getWindowMinutes(),
                Math.max(
                        config.getRules().getHighDiversity().getWindowMinutes(),
                        Math.max(
                                config.getRules().getShortCallRatio().getWindowMinutes(),
                                config.getRules().getContinuousActivity().getWindowHours() * 60
                        )
                )
        );

        // 2. RÃ©cupÃ©rer les CDR rÃ©cents
        List<Cdr> recentCdrs = windowManager.getCdrsInWindow(msisdn, maxWindowMinutes);

        // 3. Appliquer chaque rÃ¨gle et accumuler le score
        int totalScore = 0;
        List<String> triggeredRuleNames = new ArrayList<>();
        List<String> descriptions = new ArrayList<>();
        
        String maxScoreRuleName = "UNKNOWN";
        int maxScore = -1;

        for (DetectionRule rule : rules) {
            int score = rule.evaluate(msisdn, recentCdrs);
            if (score > 0) {
                totalScore += score;
                triggeredRuleNames.add(rule.getRuleName());
                descriptions.add(rule.getDescription(msisdn, recentCdrs));
                
                if (score > maxScore) {
                    maxScore = score;
                    maxScoreRuleName = rule.getRuleName();
                }
            }
        }

        // 4. Si score >= alertThreshold, on dÃ©clenche une alerte
        if (totalScore >= config.getAlertThreshold()) {
            
            // a. DÃ©terminer le RiskLevel
            RiskLevel riskLevel = RiskLevel.fromScore(totalScore);
            
            // b. DÃ©terminer l'AlertType principal (rÃ¨gle avec le plus haut score)
            AlertType alertType;
            try {
                alertType = AlertType.valueOf(maxScoreRuleName);
            } catch (IllegalArgumentException e) {
                alertType = AlertType.SIMBOX; // Fallback
            }

            try {
                String rulesJson = objectMapper.writeValueAsString(triggeredRuleNames);
                String fullDescription = String.join(" | ", descriptions);

                Alert alert = Alert.builder()
                        .alertId(UUID.randomUUID().toString())
                        .callerMsisdn(msisdn)
                        .alertType(alertType)
                        .riskScore(Math.min(totalScore, 100))
                        .riskLevel(riskLevel)
                        .triggeredRules(rulesJson)
                        .description(fullDescription)
                        .status(AlertStatus.NEW)
                        .build();

                // c. CrÃ©er et persister l'alerte via alertService
                alertService.createAlert(alert);
                log.info("ALERTE CRÃ‰Ã‰E - MSISDN: {}, Score: {}, Type: {}", msisdn, totalScore, alertType);

            } catch (Exception e) {
                log.error("Erreur lors de la crÃ©ation de l'alerte pour {}: {}", msisdn, e.getMessage());
            }
        } else {
            log.debug("Analyse OK - MSISDN: {}, Score: {}", msisdn, totalScore);
        }
    }
}

