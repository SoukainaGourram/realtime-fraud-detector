package com.telecom.fraud.service;

import com.telecom.fraud.model.Blacklist;
import com.telecom.fraud.repository.BlacklistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlacklistService {

    private final BlacklistRepository repository;

    public boolean isBlacklisted(String msisdn) {
        return repository.existsByMsisdn(msisdn);
    }

    public List<Blacklist> getAllBlockedNumbers() {
        return repository.findAll();
    }

    @Transactional
    public Blacklist blockNumber(String msisdn, String reason, String adminUser) {
        if (repository.existsByMsisdn(msisdn)) {
            throw new IllegalArgumentException("Numéro déjà blacklisté");
        }
        
        Blacklist bl = Blacklist.builder()
                .msisdn(msisdn)
                .reason(reason)
                .addedBy(adminUser)
                .build();
                
        log.info("Numéro {} blacklisté par {} pour: {}", msisdn, adminUser, reason);
        return repository.save(bl);
    }

    @Transactional
    public void unblockNumber(String msisdn) {
        repository.findByMsisdn(msisdn).ifPresent(bl -> {
            repository.delete(bl);
            log.info("Numéro {} retiré de la blacklist", msisdn);
        });
    }
}
