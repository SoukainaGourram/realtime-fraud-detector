package com.telecom.simulator.generator;

import com.telecom.simulator.config.SimulatorConfig;
import com.telecom.simulator.model.CdrEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Générateur principal de CDRs.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CdrGenerator {

    private final SimulatorConfig config;
    private final FraudPatternGenerator fraudGenerator;

    private final List<String> normalLines = new ArrayList<>();
    private final List<String> fraudLines = new ArrayList<>();
    private final Map<String, String> spamTargets = new HashMap<>();
    private final Random random = new Random();

    @PostConstruct
    public void init() {
        for (int i = 0; i < config.getNumNormalLines(); i++) {
            normalLines.add("+212600" + String.format("%06d", i));
        }
        for (int i = 0; i < config.getNumFraudLines(); i++) {
            String msisdn = "+212661" + String.format("%06d", i);
            fraudLines.add(msisdn);
            spamTargets.put(msisdn, "+212700" + String.format("%06d", random.nextInt(1000)));
        }
        log.info("Initialized CdrGenerator with {} normal lines and {} fraud lines.", normalLines.size(), fraudLines.size());
    }

    public CdrEvent generateNext() {
        if (random.nextDouble() < config.getFraudRatio()) {
            String msisdn = fraudLines.get(random.nextInt(fraudLines.size()));
            return generateFraudCdr(msisdn);
        } else {
            String msisdn = normalLines.get(random.nextInt(normalLines.size()));
            return random.nextDouble() < 0.40 ? generateNormalDataCdr(msisdn) : generateNormalVoiceCdr(msisdn);
        }
    }

    private CdrEvent generateNormalVoiceCdr(String msisdn) {
        String callType = random.nextBoolean() ? "OUTGOING" : "INCOMING";
        String callee = normalLines.get(random.nextInt(normalLines.size()));
        int duration = 30 + random.nextInt(271); // 30-300s
        double cost = 0.05 + (random.nextDouble() * 1.95); // 0.05 - 2.00
        
        return CdrEvent.builder()
                .callId(UUID.randomUUID().toString())
                .callerMsisdn(msisdn)
                .calleeMsisdn(callee)
                .startTime(DateTimeFormatter.ISO_INSTANT.format(Instant.now()))
                .durationSeconds(duration)
                .callType(callType)
                .serviceType("VOICE")
                .dataVolumeMb(0.0)
                .apn(null)
                .cellId("CELL_NORMAL")
                .cost(cost)
                .status("COMPLETED")
                .patternType("NORMAL_VOICE")
                .build();
    }

    private CdrEvent generateNormalDataCdr(String msisdn) {
        int duration = 15 + random.nextInt(180); // 15s - 3 min
        double volumeMb = 2.0 + (random.nextDouble() * 148.0); // 2 à 150 Mo
        double cost = Math.round(volumeMb * 0.005 * 100.0) / 100.0;

        return CdrEvent.builder()
                .callId(UUID.randomUUID().toString())
                .callerMsisdn(msisdn)
                .calleeMsisdn("APN_TELECOM_DATA")
                .startTime(DateTimeFormatter.ISO_INSTANT.format(Instant.now()))
                .durationSeconds(duration)
                .callType("DATA_SESSION")
                .serviceType("DATA")
                .dataVolumeMb(Math.round(volumeMb * 10.0) / 10.0)
                .apn("telecom.internet")
                .cellId("CELL_DATA_4G")
                .cost(cost)
                .status("COMPLETED")
                .patternType("NORMAL_DATA")
                .build();
    }

    private CdrEvent generateFraudCdr(String msisdn) {
        int choice = random.nextInt(6);
        return switch (choice) {
            case 0 -> fraudGenerator.generateSimboxCdr(msisdn);
            case 1 -> fraudGenerator.generateHighVolumeCdr(msisdn);
            case 2 -> fraudGenerator.generateSpamCdr(msisdn, spamTargets.get(msisdn));
            case 3 -> fraudGenerator.generateClonedSimCdr(msisdn);
            case 4 -> fraudGenerator.generateDataBurstCdr(msisdn);
            default -> fraudGenerator.generateContinuousCdr(msisdn);
        };
    }
}
