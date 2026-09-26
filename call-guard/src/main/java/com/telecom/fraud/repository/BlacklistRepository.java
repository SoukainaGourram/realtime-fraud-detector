package com.telecom.fraud.repository;

import com.telecom.fraud.model.Blacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlacklistRepository extends JpaRepository<Blacklist, Long> {
    Optional<Blacklist> findByMsisdn(String msisdn);
    boolean existsByMsisdn(String msisdn);
}
