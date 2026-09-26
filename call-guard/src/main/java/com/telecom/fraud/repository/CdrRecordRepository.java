package com.telecom.fraud.repository;

import com.telecom.fraud.model.CdrRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface CdrRecordRepository extends JpaRepository<CdrRecord, Long> {

    @Query("SELECT c FROM CdrRecord c WHERE " +
           "(:msisdn IS NULL OR c.callerMsisdn LIKE %:msisdn% OR c.calleeMsisdn LIKE %:msisdn%) AND " +
           "(:serviceType IS NULL OR c.serviceType = :serviceType) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:startDate IS NULL OR c.startTime >= :startDate) AND " +
           "(:endDate IS NULL OR c.startTime <= :endDate)")
    Page<CdrRecord> findWithFilters(
            @Param("msisdn") String msisdn,
            @Param("serviceType") String serviceType,
            @Param("status") String status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    long countByServiceType(String serviceType);

    @Query("SELECT COALESCE(SUM(c.dataVolumeMb), 0.0) FROM CdrRecord c")
    double getTotalDataVolumeMb();

    @Query("SELECT COALESCE(SUM(c.durationSeconds), 0) FROM CdrRecord c WHERE c.serviceType = 'VOICE'")
    long getTotalVoiceDurationSeconds();
}
