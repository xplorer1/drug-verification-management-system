package com.pharma.drugverification.repository;

import com.pharma.drugverification.domain.SerializedUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SerializedUnitRepository extends JpaRepository<SerializedUnit, Long> {

    Optional<SerializedUnit> findBySerialNumber(String serialNumber);

    Page<SerializedUnit> findByBatchId(Long batchId, Pageable pageable);

    List<SerializedUnit> findByBatchId(Long batchId); // Non-paginated variant for internal use

    List<SerializedUnit> findByBatchIdAndStatus(Long batchId, SerializedUnit.UnitStatus status);

    List<SerializedUnit> findByParentAggregationId(Long parentAggregationId);

    Page<SerializedUnit> findByStatus(SerializedUnit.UnitStatus status, Pageable pageable);

    boolean existsBySerialNumber(String serialNumber);

    long countByBatchId(Long batchId);

    long countByBatchIdAndStatus(Long batchId, SerializedUnit.UnitStatus status);

    @Modifying
    @Query("UPDATE SerializedUnit s SET s.status = :newStatus WHERE s.batchId = :batchId AND s.status = :oldStatus")
    int updateStatusByBatchIdAndStatus(
            @Param("batchId") Long batchId,
            @Param("oldStatus") SerializedUnit.UnitStatus oldStatus,
            @Param("newStatus") SerializedUnit.UnitStatus newStatus);
}
