package com.telecom.fraud.detection.rules;

import com.telecom.fraud.config.DetectionRulesConfig;
import com.telecom.fraud.model.Cdr;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Règle 3: Détection d'un ratio élevé d'appels très courts.
 * 
 * Les SIM Box génèrent souvent des appels qui durent 1-5 secondes
 * (tests de connectivité, signalisation). Un ratio > 70% est suspect.
 */
@Component
@RequiredArgsConstructor
public class ShortCallRatioRule implements DetectionRule {

    private final DetectionRulesConfig config;

    @Override
    public int evaluate(String msisdn, List<Cdr> recentCdrs) {
        DetectionRulesConfig.RuleConfig ruleConfig = config.getRules().getShortCallRatio();
        
        long totalOutgoing = recentCdrs.stream()
                .filter(cdr -> "OUTGOING".equals(cdr.getCallType()) || "INTERNATIONAL".equals(cdr.getCallType()))
                .count();
                
        if (totalOutgoing == 0) return 0;
        
        long shortCalls = recentCdrs.stream()
                .filter(cdr -> ("OUTGOING".equals(cdr.getCallType()) || "INTERNATIONAL".equals(cdr.getCallType()))
                        && cdr.getDurationSeconds() <= ruleConfig.getShortDurationSeconds())
                .count();
                
        double ratio = (double) shortCalls / totalOutgoing;

        if (ratio > ruleConfig.getMaxRatio()) {
            // Plus le ratio dépasse le seuil, plus le score est élevé
            double excess = ratio / ruleConfig.getMaxRatio();
            return (int) Math.min(ruleConfig.getScoreWeight() * excess, ruleConfig.getScoreWeight() * 3);
        }
        return 0;
    }

    @Override
    public String getRuleName() {
        return "SHORT_CALL_RATIO";
    }

    @Override
    public String getDescription(String msisdn, List<Cdr> cdrs) {
        DetectionRulesConfig.RuleConfig ruleConfig = config.getRules().getShortCallRatio();
        long totalOutgoing = cdrs.stream()
                .filter(cdr -> "OUTGOING".equals(cdr.getCallType()) || "INTERNATIONAL".equals(cdr.getCallType()))
                .count();
        long shortCalls = cdrs.stream()
                .filter(cdr -> ("OUTGOING".equals(cdr.getCallType()) || "INTERNATIONAL".equals(cdr.getCallType()))
                        && cdr.getDurationSeconds() <= ruleConfig.getShortDurationSeconds())
                .count();
        
        double ratio = totalOutgoing > 0 ? (double) shortCalls / totalOutgoing : 0;
        return String.format("Ratio d'appels courts élevé: %.2f%% (%d appels sur %d durent moins de %ds).", 
                ratio * 100, shortCalls, totalOutgoing, ruleConfig.getShortDurationSeconds());
    }
}
