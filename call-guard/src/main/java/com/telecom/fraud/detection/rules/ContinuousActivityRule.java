package com.telecom.fraud.detection.rules;

import com.telecom.fraud.config.DetectionRulesConfig;
import com.telecom.fraud.model.Cdr;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Règle 4: Détection d'activité continue non-humaine.
 * 
 * Un humain normal ne peut pas passer des appels 24h/24 sans pause.
 * Si une ligne est active sans interruption de plus de X minutes
 * sur une longue période, c'est probablement un automate (SIM Box).
 */
@Component
@RequiredArgsConstructor
public class ContinuousActivityRule implements DetectionRule {

    private final DetectionRulesConfig config;

    @Override
    public int evaluate(String msisdn, List<Cdr> recentCdrs) {
        DetectionRulesConfig.RuleConfig ruleConfig = config.getRules().getContinuousActivity();
        
        if (recentCdrs.size() < 10) return 0; // Pas assez de données
        
        // Trier par date de début
        List<Cdr> sortedCdrs = recentCdrs.stream()
                .sorted(Comparator.comparing(Cdr::getStartTime))
                .collect(Collectors.toList());
                
        // Calculer l'étendue temporelle
        long spanHours = ChronoUnit.HOURS.between(
                sortedCdrs.get(0).getStartTime(), 
                sortedCdrs.get(sortedCdrs.size() - 1).getStartTime());
                
        // Si l'activité ne couvre pas la fenêtre minimale requise, on ne déclenche pas
        if (spanHours < ruleConfig.getWindowHours()) {
            return 0;
        }

        // Vérifier le gap maximal entre deux appels successifs
        long maxGapMinutes = 0;
        for (int i = 1; i < sortedCdrs.size(); i++) {
            long gap = ChronoUnit.MINUTES.between(
                    sortedCdrs.get(i - 1).getStartTime(),
                    sortedCdrs.get(i).getStartTime());
            if (gap > maxGapMinutes) {
                maxGapMinutes = gap;
            }
        }

        // Si la pause maximale est inférieure au max acceptable, c'est suspect
        if (maxGapMinutes <= ruleConfig.getMaxPauseMinutes()) {
            return ruleConfig.getScoreWeight();
        }
        return 0;
    }

    @Override
    public String getRuleName() {
        return "CONTINUOUS_ACTIVITY";
    }

    @Override
    public String getDescription(String msisdn, List<Cdr> cdrs) {
        return "Activité continue détectée: pas de pause significative entre les appels sur une longue période (comportement d'automate).";
    }
}
