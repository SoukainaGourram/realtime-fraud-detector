package com.telecom.simulator.generator;

import com.telecom.simulator.model.CdrEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Générateur de patterns de fraude télécom.
 * 4 types: SIMBOX, HIGH_VOLUME, SPAM, CONTINUOUS
 */
@Component
public class FraudPatternGenerator {
    private static final List<String> CELL_IDS = List.of(
            "CELL_CASA_01", "CELL_CASA_02", "CELL_CASA_03",
            "CELL_RABAT_01", "CELL_RABAT_02", "CELL_MARRAKECH_01",
            "CELL_FES_01", "CELL_TANGER_01", "CELL_AGADIR_01"
    );
    private final Random random = new Random();

    /** SIM Box: appels courts (1-8s), INTERNATIONAL, destinataires variés */
    public CdrEvent generateSimboxCdr(String callerMsisdn) {
        return CdrEvent.builder()
                .callId(UUID.randomUUID().toString())
                .callerMsisdn(callerMsisdn)
                .calleeMsisdn(generateRandomMsisdn("INTL"))
                .startTime(DateTimeFormatter.ISO_INSTANT.format(Instant.now()))
                .durationSeconds(1 + random.nextInt(8))
                .callType("OUTGOING")
                .serviceType("VOICE")
                .cellId(randomCell())
                .cost(0.0) // SIM box bypass
                .status("COMPLETED")
                .patternType("SIMBOX")
                .build();
    }

    /** HIGH VOLUME: appels OUTGOING fréquents, durée 10-60s */
    public CdrEvent generateHighVolumeCdr(String callerMsisdn) {
        return CdrEvent.builder()
                .callId(UUID.randomUUID().toString())
                .callerMsisdn(callerMsisdn)
                .calleeMsisdn(generateRandomMsisdn("LOCAL"))
                .startTime(DateTimeFormatter.ISO_INSTANT.format(Instant.now()))
                .durationSeconds(10 + random.nextInt(51))
                .callType("OUTGOING")
                .serviceType("VOICE")
                .cellId(randomCell())
                .cost(0.1 + (random.nextDouble() * 0.9))
                .status("COMPLETED")
                .patternType("HIGH_VOLUME")
                .build();
    }

    /** SPAM: même callee répété, appels courts */
    public CdrEvent generateSpamCdr(String callerMsisdn, String calleeMsisdn) {
        return CdrEvent.builder()
                .callId(UUID.randomUUID().toString())
                .callerMsisdn(callerMsisdn)
                .calleeMsisdn(calleeMsisdn)
                .startTime(DateTimeFormatter.ISO_INSTANT.format(Instant.now()))
                .durationSeconds(1 + random.nextInt(5))
                .callType("OUTGOING")
                .serviceType("VOICE")
                .cellId(randomCell())
                .cost(0.05)
                .status("COMPLETED")
                .patternType("SPAM")
                .build();
    }

    /** CONTINUOUS: appels à toute heure, sans pause, durée variée */
    public CdrEvent generateContinuousCdr(String callerMsisdn) {
        return CdrEvent.builder()
                .callId(UUID.randomUUID().toString())
                .callerMsisdn(callerMsisdn)
                .calleeMsisdn(generateRandomMsisdn("LOCAL"))
                .startTime(DateTimeFormatter.ISO_INSTANT.format(Instant.now()))
                .durationSeconds(60 + random.nextInt(300))
                .callType("OUTGOING")
                .serviceType("VOICE")
                .cellId(randomCell())
                .cost(1.0 + (random.nextDouble() * 2.0))
                .status("COMPLETED")
                .patternType("CONTINUOUS")
                .build();
    }

    /** CLONED SIM: Appels alternés très rapides entre CASA et RABAT */
    public CdrEvent generateClonedSimCdr(String callerMsisdn) {
        // Alterne entre CASA et RABAT à chaque appel
        String cell = random.nextBoolean() ? "CELL_CASA_01" : "CELL_RABAT_01";
        return CdrEvent.builder()
                .callId(UUID.randomUUID().toString())
                .callerMsisdn(callerMsisdn)
                .calleeMsisdn(generateRandomMsisdn("LOCAL"))
                .startTime(DateTimeFormatter.ISO_INSTANT.format(Instant.now()))
                .durationSeconds(5 + random.nextInt(15))
                .callType("OUTGOING")
                .serviceType("VOICE")
                .cellId(cell)
                .cost(0.1)
                .status("COMPLETED")
                .patternType("CLONED_SIM")
                .build();
    }

    /** DATA BURST: Consommation massive anormale de données (fuite data / usage illégal) */
    public CdrEvent generateDataBurstCdr(String callerMsisdn) {
        double burstMb = 800.0 + (random.nextDouble() * 2200.0); // 800 à 3000 Mo
        return CdrEvent.builder()
                .callId(UUID.randomUUID().toString())
                .callerMsisdn(callerMsisdn)
                .calleeMsisdn("APN_GATEWAY")
                .startTime(DateTimeFormatter.ISO_INSTANT.format(Instant.now()))
                .durationSeconds(120 + random.nextInt(600))
                .callType("DATA_SESSION")
                .serviceType("DATA")
                .dataVolumeMb(Math.round(burstMb * 10.0) / 10.0)
                .apn("telecom.internet")
                .cellId(randomCell())
                .cost(Math.round(burstMb * 0.02 * 100.0) / 100.0)
                .status("COMPLETED")
                .patternType("DATA_BURST")
                .build();
    }

    private String generateRandomMsisdn(String type) {
        if ("INTL".equals(type)) {
            String[] prefixes = {"+33", "+34", "+1"};
            String prefix = prefixes[random.nextInt(prefixes.length)];
            return prefix + String.format("%09d", random.nextInt(1000000000));
        } else {
            String[] prefixes = {"+212600", "+212661", "+212700"};
            String prefix = prefixes[random.nextInt(prefixes.length)];
            return prefix + String.format("%06d", random.nextInt(1000000));
        }
    }

    private String randomCell() {
        return CELL_IDS.get(random.nextInt(CELL_IDS.size()));
    }
}
