package com.pharma.drugverification.repository;

import com.pharma.drugverification.domain.VerificationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, Long> {

    List<VerificationRequest> findBySerialNumber(String serialNumber);

    Page<VerificationRequest> findBySerializedUnitId(Long serializedUnitId, Pageable pageable);

    Page<VerificationRequest> findByResult(VerificationRequest.VerificationResult result, Pageable pageable);

    @Query("SELECT vr FROM VerificationRequest vr WHERE vr.serialNumber = :serialNumber " +
            "AND vr.createdAt BETWEEN :startTime AND :endTime ORDER BY vr.createdAt DESC")
    List<VerificationRequest> findRecentVerificationsBySerial(
            String serialNumber,
            LocalDateTime startTime,
            LocalDateTime endTime);

    @Query("SELECT AVG(vr.responseTimeMs) FROM VerificationRequest vr " +
            "WHERE vr.createdAt >= :since")
    Double getAverageResponseTime(LocalDateTime since);

    long countByResultAndCreatedAtAfter(
            VerificationRequest.VerificationResult result,
            LocalDateTime since);
}
