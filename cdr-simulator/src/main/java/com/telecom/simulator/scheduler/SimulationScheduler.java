package com.telecom.simulator.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telecom.simulator.config.SimulatorConfig;
import com.telecom.simulator.generator.CdrGenerator;
import com.telecom.simulator.model.CdrEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Planificateur de simulation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SimulationScheduler {

    private final CdrGenerator cdrGenerator;
    private final SimulatorConfig config;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicLong counter = new AtomicLong(0);

    @Scheduled(fixedRate = 100)
    public void simulate() {
        int cdrsPerTick = config.getRatePerSecond() / 10;
        if (cdrsPerTick == 0) cdrsPerTick = 1;

        for (int i = 0; i < cdrsPerTick; i++) {
            CdrEvent event = cdrGenerator.generateNext();
            try {
                String json = objectMapper.writeValueAsString(event);
                kafkaTemplate.send(config.getKafkaTopic(), event.getCallerMsisdn(), json);
                
                long currentCount = counter.incrementAndGet();
                if (currentCount % 100 == 0) {
                    log.info("Sent {} CDRs to Kafka", currentCount);
                }
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize CDR event", e);
            }
        }
    }
}
