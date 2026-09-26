package com.telecom.fraud.detection.rules;

import com.telecom.fraud.config.DetectionRulesConfig;
import com.telecom.fraud.model.Cdr;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Règle 2: Détection de diversité anormale de destinataires.
 * 
 * Une ligne SIM Box appelle un très grand nombre de destinataires différents
 * (tests de lignes actives, bypass international).
 */
@Component
@RequiredArgsConstructor
public class HighDiversityRule implements DetectionRule {

    private final DetectionRulesConfig config;

    @Override
    public int evaluate(String msisdn, List<Cdr> recentCdrs) {
        DetectionRulesConfig.RuleConfig ruleConfig = config.getRules().getHighDiversity();
        
        long distinctCallees = recentCdrs.stream()
                .filter(cdr -> "OUTGOING".equals(cdr.getCallType()) || "INTERNATIONAL".equals(cdr.getCallType()))
                .map(Cdr::getCalleeMsisdn)
                .distinct()
                .count();

        if (distinctCallees > ruleConfig.getMaxDistinctCallees()) {
            double ratio = (double) distinctCallees / ruleConfig.getMaxDistinctCallees();
            return (int) Math.min(ruleConfig.getScoreWeight() * ratio, ruleConfig.getScoreWeight() * 3);
        }
        return 0;
    }

    @Override
    public String getRuleName() {
        return "HIGH_DIVERSITY";
    }

    @Override
    public String getDescription(String msisdn, List<Cdr> cdrs) {
        long distinctCallees = cdrs.stream()
                .filter(cdr -> "OUTGOING".equals(cdr.getCallType()) || "INTERNATIONAL".equals(cdr.getCallType()))
                .map(Cdr::getCalleeMsisdn)
                .distinct()
                .count();
        return String.format("Diversité anormale détectée: %d destinataires distincts appelés.", distinctCallees);
    }
}
