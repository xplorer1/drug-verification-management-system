package com.pharma.drugverification.service;

import com.pharma.drugverification.domain.Batch;
import com.pharma.drugverification.domain.Drug;
import com.pharma.drugverification.domain.StatusTransition;
import com.pharma.drugverification.dto.BatchCreationRequest;
import com.pharma.drugverification.dto.BatchResponse;
import com.pharma.drugverification.dto.BatchUpdateRequest;
import com.pharma.drugverification.repository.BatchRepository;
import com.pharma.drugverification.repository.DrugRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchService {

    private final BatchRepository batchRepository;
    private final DrugRepository drugRepository;
    private final StatusTransitionService statusTransitionService;
    private final AuditService auditService;

    @Transactional
    @CacheEvict(value = "batches", allEntries = true)
    public BatchResponse createBatch(BatchCreationRequest request, Long userId) {
        if (batchRepository.existsByBatchNumber(request.getBatchNumber())) {
            throw new RuntimeException("Batch with number " + request.getBatchNumber() + " already exists");
        }

        Drug drug = drugRepository.findById(request.getDrugId())
                .orElseThrow(() -> new RuntimeException("Drug not found"));

        if (drug.getStatus() != Drug.DrugStatus.APPROVED) {
            throw new RuntimeException("Cannot create batch for unapproved drug");
        }

        if (request.getExpirationDate().isBefore(request.getManufacturingDate())) {
            throw new RuntimeException("Expiration date must be after manufacturing date");
        }

        Batch batch = new Batch();
        batch.setBatchNumber(request.getBatchNumber());
        batch.setDrugId(request.getDrugId());
        batch.setManufacturingDate(request.getManufacturingDate());
        batch.setExpirationDate(request.getExpirationDate());
        batch.setQuantity(request.getQuantity());
        batch.setStatus(Batch.BatchStatus.ACTIVE);
        batch.setLocation(request.getLocation());
        batch.setNotes(request.getNotes());

        Batch saved = batchRepository.save(batch);

        auditService.log("BATCH_CREATED", "Batch", saved.getId(), userId,
                Map.of("batchNumber", request.getBatchNumber(), "drugId", request.getDrugId()));

        return BatchResponse.from(saved);
    }

    @Transactional
    @CacheEvict(value = "batches", allEntries = true)
    public BatchResponse updateBatch(Long batchId, BatchUpdateRequest request, Long userId) {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        batch.setLocation(request.getLocation());
        batch.setNotes(request.getNotes());
        batch.setQuantity(request.getQuantity());

        Batch saved = batchRepository.save(batch);

        auditService.log("BATCH_UPDATED", "Batch", batchId, userId,
                Map.of("batchNumber", batch.getBatchNumber()));

        return BatchResponse.from(saved);
    }

    @Transactional
    @CacheEvict(value = "batches", allEntries = true)
    public BatchResponse updateBatchStatus(Long batchId, Batch.BatchStatus newStatus, String reason, Long userId) {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        Batch.BatchStatus oldStatus = batch.getStatus();
        batch.setStatus(newStatus);

        Batch saved = batchRepository.save(batch);

        statusTransitionService.recordTransition(
                StatusTransition.EntityType.BATCH,
                batchId,
                oldStatus.name(),
                newStatus.name(),
                reason,
                userId);

        auditService.log("BATCH_STATUS_CHANGED", "Batch", batchId, userId,
                Map.of("batchNumber", batch.getBatchNumber(), "fromStatus", oldStatus, "toStatus", newStatus));

        return BatchResponse.from(saved);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "batches", key = "#id")
    public BatchResponse getBatchById(Long id) {
        Batch batch = batchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Batch not found"));
        return BatchResponse.from(batch);
    }

    @Transactional(readOnly = true)
    public BatchResponse getBatchByNumber(String batchNumber) {
        Batch batch = batchRepository.findByBatchNumber(batchNumber)
                .orElseThrow(() -> new RuntimeException("Batch not found"));
        return BatchResponse.from(batch);
    }

    @Transactional(readOnly = true)
    public Page<BatchResponse> getBatchesByDrug(Long drugId, Pageable pageable) {
        return batchRepository.findByDrugId(drugId, pageable)
                .map(BatchResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<BatchResponse> getBatchesByStatus(Batch.BatchStatus status, Pageable pageable) {
        return batchRepository.findByStatus(status, pageable)
                .map(BatchResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<BatchResponse> getAllBatches(Pageable pageable) {
        return batchRepository.findAll(pageable)
                .map(BatchResponse::from);
    }

    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    @Transactional
    public void markExpiredBatches() {
        LocalDate today = LocalDate.now();
        List<Batch> expiredBatches = batchRepository.findExpiredBatches(today);

        for (Batch batch : expiredBatches) {
            Batch.BatchStatus oldStatus = batch.getStatus();
            batch.setStatus(Batch.BatchStatus.EXPIRED);
            batchRepository.save(batch);

            statusTransitionService.recordTransition(
                    StatusTransition.EntityType.BATCH,
                    batch.getId(),
                    oldStatus.name(),
                    Batch.BatchStatus.EXPIRED.name(),
                    "Automatically expired based on expiration date",
                    1L // System user ID
            );

            log.info("Marked batch {} as expired", batch.getBatchNumber());
        }

        if (!expiredBatches.isEmpty()) {
            log.info("Marked {} batches as expired", expiredBatches.size());
        }
    }
}
