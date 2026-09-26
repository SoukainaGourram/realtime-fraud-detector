package com.telecom.fraud.api;

import com.telecom.fraud.model.CdrRecord;
import com.telecom.fraud.repository.CdrRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Contrôleur REST pour la consultation et la recherche dans l'historique des CDR (Voix + Data).
 * Correspond au premier sujet (Consultation d'historique) intégré dans la plateforme.
 */
@RestController
@RequestMapping("/api/v1/cdrs/history")
@RequiredArgsConstructor
@Slf4j
public class CdrHistoryController {

    private final CdrRecordRepository cdrRecordRepository;

    /**
     * Recherche multicritère avec pagination dans l'historique des CDR.
     */
    @GetMapping
    public ResponseEntity<Page<CdrRecord>> searchCdrs(
            @RequestParam(required = false) String msisdn,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "startTime") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        String cleanMsisdn = (msisdn != null && !msisdn.trim().isEmpty()) ? msisdn.trim() : null;
        String cleanService = (serviceType != null && !serviceType.trim().isEmpty()) ? serviceType.trim().toUpperCase() : null;
        String cleanStatus = (status != null && !status.trim().isEmpty()) ? status.trim().toUpperCase() : null;

        Page<CdrRecord> results = cdrRecordRepository.findWithFilters(cleanMsisdn, cleanService, cleanStatus, startDate, endDate, pageable);
        return ResponseEntity.ok(results);
    }

    /**
     * Statistiques globales sur les CDR stockés (répartition Voix vs Data, volumes).
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCdrStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        long total = cdrRecordRepository.count();
        long voice = cdrRecordRepository.countByServiceType("VOICE");
        long data = cdrRecordRepository.countByServiceType("DATA");
        double dataMb = cdrRecordRepository.getTotalDataVolumeMb();
        long voiceSec = cdrRecordRepository.getTotalVoiceDurationSeconds();

        stats.put("totalCdrs", total);
        stats.put("voiceCount", voice);
        stats.put("dataCount", data);
        stats.put("totalDataVolumeMb", Math.round(dataMb * 100.0) / 100.0);
        stats.put("totalVoiceDurationMinutes", voiceSec / 60);

        return ResponseEntity.ok(stats);
    }

    /**
     * Consultation du détail d'un CDR par son ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CdrRecord> getCdrById(@PathVariable Long id) {
        return cdrRecordRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
