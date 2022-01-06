package com.pharma.drugverification.repository;

import com.pharma.drugverification.domain.SerializedUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SerializedUnitRepository extends JpaRepository<SerializedUnit, Long> {

    Optional<SerializedUnit> findBySerialNumber(String serialNumber);

    List<SerializedUnit> findByBatchId(Long batchId);

    List<SerializedUnit> findByBatchIdAndStatus(Long batchId, SerializedUnit.UnitStatus status);

    List<SerializedUnit> findByParentAggregationId(Long parentAggregationId);

    List<SerializedUnit> findByStatus(SerializedUnit.UnitStatus status);

    boolean existsBySerialNumber(String serialNumber);

    long countByBatchId(Long batchId);

    long countByBatchIdAndStatus(Long batchId, SerializedUnit.UnitStatus status);
}
