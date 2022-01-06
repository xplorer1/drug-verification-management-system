package com.pharma.drugverification.repository;

import com.pharma.drugverification.domain.Aggregation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AggregationRepository extends JpaRepository<Aggregation, Long> {

    List<Aggregation> findByParentIdAndActiveTrue(Long parentId);

    List<Aggregation> findByChildIdAndActiveTrue(Long childId);

    List<Aggregation> findByBatchIdAndActiveTrue(Long batchId);

    boolean existsByChildIdAndActiveTrue(Long childId);
}
