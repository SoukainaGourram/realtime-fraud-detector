package com.telecom.fraud.detection;

import com.telecom.fraud.model.Cdr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestionnaire de fenêtres glissantes temporelles.
 * 
 * Pour chaque MSISDN (numéro de téléphone), maintient une file des CDR
 * reçus récemment. Permet d'analyser le comportement sur une fenêtre
 * de temps glissante (ex: dernières 10 minutes).
 *
 * Utilise ConcurrentHashMap pour la thread-safety (plusieurs consumers Kafka).
 */
@Component
@Slf4j
public class SlidingWindowManager {
    
    // Structure: Map<String (MSISDN), Deque<Cdr>>
    private final ConcurrentHashMap<String, ArrayDeque<Cdr>> windows = new ConcurrentHashMap<>();

    /**
     * Ajoute un CDR dans la fenêtre de l'appelant.
     * @param cdr Le CDR à ajouter
     */
    public void addCdr(Cdr cdr) {
        if (cdr == null || cdr.getCallerMsisdn() == null) return;
        
        windows.compute(cdr.getCallerMsisdn(), (key, deque) -> {
            if (deque == null) {
                deque = new ArrayDeque<>();
            }
            deque.addLast(cdr);
            return deque;
        });
    }

    /**
     * Retourne la liste des CDR dans les X dernières minutes et supprime les anciens.
     * @param msisdn Le numéro de l'appelant
     * @param windowMinutes La taille de la fenêtre en minutes
     * @return La liste des CDR récents
     */
    public List<Cdr> getCdrsInWindow(String msisdn, int windowMinutes) {
        ArrayDeque<Cdr> deque = windows.get(msisdn);
        if (deque == null || deque.isEmpty()) {
            return new ArrayList<>();
        }

        Instant threshold = Instant.now().minus(windowMinutes, ChronoUnit.MINUTES);
        
        synchronized (deque) {
            // Eviction des CDR trop anciens
            while (!deque.isEmpty() && deque.peekFirst().getStartTime().isBefore(threshold)) {
                deque.pollFirst();
            }
            return new ArrayList<>(deque);
        }
    }

    /**
     * Nettoyage périodique pour éviter les fuites de mémoire.
     * Supprime les MSISDN inactifs depuis plus de 2 heures.
     * Exécuté toutes les minutes.
     */
    @Scheduled(fixedRate = 60000)
    public void cleanupOldEntries() {
        Instant threshold = Instant.now().minus(2, ChronoUnit.HOURS);
        int initialSize = windows.size();
        
        windows.entrySet().removeIf(entry -> {
            ArrayDeque<Cdr> deque = entry.getValue();
            synchronized (deque) {
                if (deque.isEmpty()) return true;
                return deque.peekLast().getStartTime().isBefore(threshold);
            }
        });
        
        int removed = initialSize - windows.size();
        if (removed > 0) {
            log.debug("Nettoyage: {} fenêtres inactives supprimées", removed);
        }
    }
}
