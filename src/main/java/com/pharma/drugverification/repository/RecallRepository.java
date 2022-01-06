package com.pharma.drugverification.repository;

import com.pharma.drugverification.domain.Recall;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecallRepository extends JpaRepository<Recall, Long> {

    Optional<Recall> findByRecallNumber(String recallNumber);

    Page<Recall> findByStatus(Recall.RecallStatus status, Pageable pageable);

    List<Recall> findByBatchId(Long batchId);

    Page<Recall> findByClassification(Recall.RecallClassification classification, Pageable pageable);

    List<Recall> findByStatusAndClassification(
            Recall.RecallStatus status,
            Recall.RecallClassification classification);

    boolean existsByRecallNumber(String recallNumber);

    boolean existsByBatchIdAndStatus(Long batchId, Recall.RecallStatus status);
}
