package com.telecom.fraud.detection.rules;

import com.telecom.fraud.model.Cdr;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Règle de géolocalisation impossible (SIM Cloning).
 * Si un numéro émet un appel depuis Casablanca puis depuis Rabat (distance > 80km)
 * en moins de 10 minutes, c'est physiquement impossible : la carte SIM a été clonée.
 */
@Component
public class GeoImpossibleRule implements DetectionRule {

    // Simulation simple des distances (vrai système utiliserait des coordonnées GPS)
    private final Map<String, String> cities = Map.of(
            "CELL_CASA_01", "CASA",
            "CELL_CASA_02", "CASA",
            "CELL_RABAT_01", "RABAT",
            "CELL_RABAT_02", "RABAT",
            "CELL_MARRAKECH_01", "MARRAKECH",
            "CELL_TANGER_01", "TANGER",
            "CELL_AGADIR_01", "AGADIR",
            "CELL_FES_01", "FES"
    );

    @Override
    public int evaluate(String msisdn, List<Cdr> recentCdrs) {
        if (recentCdrs.size() < 2) return 0;

        Cdr latest = recentCdrs.getLast();
        String latestCity = getCity(latest.getCellId());
        if (latestCity == null) return 0;

        // On cherche un CDR récent depuis une AUTRE ville
        for (int i = recentCdrs.size() - 2; i >= 0; i--) {
            Cdr prev = recentCdrs.get(i);
            String prevCity = getCity(prev.getCellId());
            
            if (prevCity != null && !prevCity.equals(latestCity)) {
                long minutesBetween = Math.abs(Duration.between(prev.getStartTime(), latest.getStartTime()).toMinutes());
                
                // Différente ville en moins de 30 minutes = impossible
                if (minutesBetween < 30) {
                    return 100; // CRITICAL direct
                }
            }
        }

        return 0;
    }

    private String getCity(String cellId) {
        return cellId == null ? null : cities.getOrDefault(cellId, null);
    }

    @Override
    public String getRuleName() {
        return "CLONED_SIM";
    }

    @Override
    public String getDescription(String msisdn, List<Cdr> cdrs) {
        return "Impossible Travel détecté (SIM Clonée): Appels depuis différentes villes en moins de 30min.";
    }
}
