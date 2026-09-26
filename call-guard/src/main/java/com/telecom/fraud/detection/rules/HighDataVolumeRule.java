package com.telecom.fraud.detection.rules;

import com.telecom.fraud.config.DetectionRulesConfig;
import com.telecom.fraud.model.Cdr;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Règle de Détection Data: Consommation de données anormale (Data Surge / Burst).
 * 
 * Détecte les pics massifs ou inattendus de consommation de données internet (sessions DATA)
 * sur une courte période pour un MSISDN donné.
 */
@Component
@RequiredArgsConstructor
public class HighDataVolumeRule implements DetectionRule {

    private final DetectionRulesConfig config;

    @Override
    public int evaluate(String msisdn, List<Cdr> recentCdrs) {
        DetectionRulesConfig.RuleConfig ruleConfig = config.getRules().getHighDataVolume();
        double thresholdMb = ruleConfig.getMaxDataMb() > 0 ? ruleConfig.getMaxDataMb() : 1500.0;
        int baseWeight = ruleConfig.getScoreWeight() > 0 ? ruleConfig.getScoreWeight() : 35;

        double totalDataMb = recentCdrs.stream()
                .filter(cdr -> "DATA".equalsIgnoreCase(cdr.getServiceType()) || cdr.getDataVolumeMb() > 0)
                .mapToDouble(Cdr::getDataVolumeMb)
                .sum();

        if (totalDataMb > thresholdMb) {
            double ratio = totalDataMb / thresholdMb;
            return (int) Math.min(baseWeight * ratio, baseWeight * 2.5);
        }
        return 0;
    }

    @Override
    public String getRuleName() {
        return "HIGH_DATA_VOLUME";
    }

    @Override
    public String getDescription(String msisdn, List<Cdr> cdrs) {
        double totalDataMb = cdrs.stream()
                .filter(cdr -> "DATA".equalsIgnoreCase(cdr.getServiceType()) || cdr.getDataVolumeMb() > 0)
                .mapToDouble(Cdr::getDataVolumeMb)
                .sum();
        return String.format("Surconsommation de données anormale: %.1f Mo consommés sur la période récente.", totalDataMb);
    }
}
