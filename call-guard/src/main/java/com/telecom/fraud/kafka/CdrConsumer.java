package com.telecom.fraud.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telecom.fraud.detection.DetectionEngine;
import com.telecom.fraud.model.Cdr;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Consumer Kafka pour le topic 'cdrs'.
 *
 * Reçoit les CDR en temps réel et les transmet au moteur de détection.
 * En cas d'erreur de désérialisation ou de traitement, le message est
 * renvoyé dans un topic Dead Letter Queue (cdrs-dlq) pour ne rien perdre.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CdrConsumer {

    private static final String DLQ_TOPIC = "cdrs-dlq";

    private final DetectionEngine detectionEngine;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final com.telecom.fraud.repository.CdrRecordRepository cdrRecordRepository;

    // Compteurs pour le monitoring
    private final AtomicLong processedCount = new AtomicLong(0);
    private final AtomicLong dlqCount       = new AtomicLong(0);

    @KafkaListener(topics = "cdrs", groupId = "fraud-detection-group")
    public void consume(String message) {
        try {
            Cdr cdr = objectMapper.readValue(message, Cdr.class);
            log.debug("CDR reçu pour MSISDN: {}", cdr.getCallerMsisdn());
            
            // 1. Analyse temps réel pour détection de fraude
            detectionEngine.analyze(cdr);

            // 2. Persistance dans l'historique (1er sujet - Consultation CDR)
            try {
                com.telecom.fraud.model.CdrRecord record = com.telecom.fraud.model.CdrRecord.fromCdr(cdr);
                cdrRecordRepository.save(record);
            } catch (Exception dbEx) {
                log.warn("Erreur archivage CDR historique: {}", dbEx.getMessage());
            }

            processedCount.incrementAndGet();
        } catch (Exception e) {
            // ─── Dead Letter Queue ───────────────────────────────────────────
            // Le message est mal formé ou le traitement a échoué.
            // On l'envoie dans cdrs-dlq pour investigation ultérieure,
            // au lieu de le perdre définitivement.
            log.error("Erreur traitement CDR → envoi en DLQ. Cause: {}", e.getMessage());
            try {
                kafkaTemplate.send(DLQ_TOPIC, message);
                dlqCount.incrementAndGet();
            } catch (Exception dlqEx) {
                log.error("Impossible d'envoyer en DLQ: {}", dlqEx.getMessage());
            }
        }
    }

    /** Retourne le nombre total de CDR traités avec succès (pour le monitoring). */
    public long getProcessedCount() { return processedCount.get(); }

    /** Retourne le nombre de messages envoyés en DLQ (indicateur d'erreurs). */
    public long getDlqCount() { return dlqCount.get(); }
}
