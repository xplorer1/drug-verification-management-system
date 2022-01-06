package com.pharma.drugverification.repository;

import com.pharma.drugverification.domain.TelemetryReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TelemetryReadingRepository extends JpaRepository<TelemetryReading, Long> {

    List<TelemetryReading> findByBatchIdOrderByTimestampDesc(Long batchId);

    @Query("SELECT tr FROM TelemetryReading tr WHERE tr.batchId = :batchId " +
            "AND tr.timestamp BETWEEN :startTime AND :endTime ORDER BY tr.timestamp")
    List<TelemetryReading> findByBatchIdAndTimeRange(
            Long batchId,
            LocalDateTime startTime,
            LocalDateTime endTime);

    @Query("SELECT tr FROM TelemetryReading tr WHERE tr.batchId = :batchId " +
            "ORDER BY tr.timestamp DESC LIMIT 1")
    TelemetryReading findLatestByBatchId(Long batchId);

    List<TelemetryReading> findByDeviceId(String deviceId);
}
