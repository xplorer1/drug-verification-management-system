package com.pharma.drugverification.repository;

import com.pharma.drugverification.domain.TemperatureExcursion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemperatureExcursionRepository extends JpaRepository<TemperatureExcursion, Long> {

    List<TemperatureExcursion> findByBatchId(Long batchId);

    List<TemperatureExcursion> findByBatchIdAndResolvedFalse(Long batchId);

    List<TemperatureExcursion> findBySeverity(TemperatureExcursion.Severity severity);

    List<TemperatureExcursion> findByBatchIdAndSeverity(
            Long batchId,
            TemperatureExcursion.Severity severity);

    long countByBatchIdAndResolvedFalse(Long batchId);
}
