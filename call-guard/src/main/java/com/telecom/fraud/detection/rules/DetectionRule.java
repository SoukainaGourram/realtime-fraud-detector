package com.telecom.fraud.detection.rules;

import com.telecom.fraud.model.Cdr;

import java.util.List;

/**
 * Interface que toutes les règles de détection doivent implémenter.
 */
public interface DetectionRule {
    /**
     * Évalue la règle sur la fenêtre de CDR d'un MSISDN.
     * @param msisdn Le numéro de téléphone analysé
     * @param recentCdrs La liste des CDR récents dans la fenêtre
     * @return Le score ajouté par cette règle (0 si non déclenchée)
     */
    int evaluate(String msisdn, List<Cdr> recentCdrs);
    
    /** Retourne le nom de la règle pour les logs et alertes */
    String getRuleName();
    
    /** Retourne la description lisible de ce qui a été détecté */
    String getDescription(String msisdn, List<Cdr> cdrs);
}
