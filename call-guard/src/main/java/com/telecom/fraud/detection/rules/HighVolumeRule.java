package com.telecom.fraud.detection.rules;

import com.telecom.fraud.config.DetectionRulesConfig;
import com.telecom.fraud.model.Cdr;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Règle 1: Détection de volume anormal d'appels sortants.
 * 
 * Signature de fraude SIM Box: une ligne génère un volume très élevé
 * d'appels sortants sur une courte période.
 */
@Component
@RequiredArgsConstructor
public class HighVolumeRule implements DetectionRule {

    private final DetectionRulesConfig config;

    @Override
    public int evaluate(String msisdn, List<Cdr> recentCdrs) {
        DetectionRulesConfig.RuleConfig ruleConfig = config.getRules().getHighVolume();
        
        long count = recentCdrs.stream()
                .filter(cdr -> "OUTGOING".equals(cdr.getCallType()) || "INTERNATIONAL".equals(cdr.getCallType()))
                .count();

        if (count > ruleConfig.getMaxCalls()) {
            // Scoring proportionnel : ratio de dépassement, plafonné à 3x le poids de base
            double ratio = (double) count / ruleConfig.getMaxCalls();
            return (int) Math.min(ruleConfig.getScoreWeight() * ratio, ruleConfig.getScoreWeight() * 3);
        }
        return 0;
    }

    @Override
    public String getRuleName() {
        return "HIGH_VOLUME";
    }

    @Override
    public String getDescription(String msisdn, List<Cdr> cdrs) {
        long count = cdrs.stream()
                .filter(cdr -> "OUTGOING".equals(cdr.getCallType()) || "INTERNATIONAL".equals(cdr.getCallType()))
                .count();
        return String.format("Volume d'appels élevé détecté: %d appels sortants/internationaux.", count);
    }
}
