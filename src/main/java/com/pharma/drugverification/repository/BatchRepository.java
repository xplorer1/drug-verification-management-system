package com.pharma.drugverification.repository;

import com.pharma.drugverification.domain.Batch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Long> {

    Optional<Batch> findByBatchNumber(String batchNumber);

    Page<Batch> findByDrugId(Long drugId, Pageable pageable);

    Page<Batch> findByStatus(Batch.BatchStatus status, Pageable pageable);

    List<Batch> findByDrugIdAndStatus(Long drugId, Batch.BatchStatus status);

    @Query("SELECT b FROM Batch b WHERE b.expirationDate < :currentDate AND b.status != 'EXPIRED'")
    List<Batch> findExpiredBatches(LocalDate currentDate);

    boolean existsByBatchNumber(String batchNumber);
}
