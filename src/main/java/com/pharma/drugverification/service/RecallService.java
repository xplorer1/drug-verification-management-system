package com.pharma.drugverification.service;

import com.pharma.drugverification.domain.Batch;
import com.pharma.drugverification.domain.Recall;
import com.pharma.drugverification.domain.SerializedUnit;
import com.pharma.drugverification.dto.RecallRequest;
import com.pharma.drugverification.dto.RecallResponse;
import com.pharma.drugverification.repository.BatchRepository;
import com.pharma.drugverification.repository.RecallRepository;
import com.pharma.drugverification.repository.SerializedUnitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pharma.drugverification.exception.BadRequestException;
import com.pharma.drugverification.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecallService {

    private final RecallRepository recallRepository;
    private final BatchRepository batchRepository;
    private final SerializedUnitRepository serializedUnitRepository;
    private final AuditService auditService;
    private final AlertService alertService;

    @Transactional
    public RecallResponse initiateRecall(RecallRequest request, Long regulatorId) {
        Batch batch = batchRepository.findById(request.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));

        // Check if there's already an active recall for this batch
        if (recallRepository.existsByBatchIdAndStatus(request.getBatchId(), Recall.RecallStatus.ACTIVE)) {
            throw new BadRequestException("There is already an active recall for this batch");
        }

        // Count affected units
        long totalAffected = serializedUnitRepository.countByBatchId(request.getBatchId());

        Recall recall = new Recall();
        recall.setBatchId(request.getBatchId());
        recall.setClassification(request.getClassification());
        recall.setStatus(Recall.RecallStatus.ACTIVE);
        recall.setReason(request.getReason());
        recall.setAffectedUnits((int) totalAffected);
        recall.setRecoveredUnits(0);
        recall.setEffectiveness(0.0);
        recall.setInitiatedByUserId(regulatorId);

        Recall saved = recallRepository.save(recall);

        // Quarantine all active units in the batch using bulk update
        int quarantinedCount = serializedUnitRepository.updateStatusByBatchIdAndStatus(
                request.getBatchId(),
                SerializedUnit.UnitStatus.ACTIVE,
                SerializedUnit.UnitStatus.QUARANTINED);

        // Create high-priority alert
        alertService.createAlert(
                "RECALL_INITIATED",
                "High",
                String.format("Recall initiated for batch %s: %s", batch.getBatchNumber(), request.getReason()),
                "Recall",
                saved.getId());

        auditService.log("RECALL_INITIATED", "Recall", saved.getId(), regulatorId,
                Map.of("batchId", request.getBatchId(), "classification", request.getClassification().name(),
                        "totalAffected", totalAffected, "quarantinedCount", quarantinedCount));

        log.warn("Recall initiated for batch {} with {} units affected ({} quarantined)",
                batch.getBatchNumber(), totalAffected, quarantinedCount);

        return RecallResponse.from(saved);
    }

    @Transactional
    public RecallResponse recordRecovery(Long recallId, Long unitId, Long userId) {
        Recall recall = recallRepository.findById(recallId)
                .orElseThrow(() -> new ResourceNotFoundException("Recall not found"));

        if (recall.getStatus() != Recall.RecallStatus.ACTIVE) {
            throw new BadRequestException("Recall is not active");
        }

        SerializedUnit unit = serializedUnitRepository.findById(unitId)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found"));

        if (unit.getStatus() != SerializedUnit.UnitStatus.QUARANTINED) {
            throw new BadRequestException("Unit is not quarantined");
        }

        // Mark unit as destroyed
        unit.setStatus(SerializedUnit.UnitStatus.DESTROYED);
        serializedUnitRepository.save(unit);

        // Update recall statistics
        recall.setRecoveredUnits(recall.getRecoveredUnits() + 1);
        recall.setEffectiveness(
                (recall.getRecoveredUnits() * 100.0) / recall.getAffectedUnits());

        Recall saved = recallRepository.save(recall);

        auditService.log("RECALL_UNIT_RECOVERED", "Recall", recallId, userId,
                Map.of("unitId", unitId, "serialNumber", unit.getSerialNumber()));

        return RecallResponse.from(saved);
    }

    @Transactional
    public RecallResponse completeRecall(Long recallId, Long userId) {
        Recall recall = recallRepository.findById(recallId)
                .orElseThrow(() -> new ResourceNotFoundException("Recall not found"));

        if (recall.getStatus() != Recall.RecallStatus.ACTIVE) {
            throw new BadRequestException("Recall is not active");
        }

        recall.setStatus(Recall.RecallStatus.COMPLETED);
        recall.setClosedAt(LocalDateTime.now());
        recall.setClosedByUserId(userId);

        Recall saved = recallRepository.save(recall);

        auditService.log("RECALL_COMPLETED", "Recall", recallId, userId,
                Map.of("effectiveness", recall.getEffectiveness()));

        log.info("Recall {} completed with {}% effectiveness",
                recallId, String.format("%.2f", recall.getEffectiveness()));

        return RecallResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public RecallResponse getRecall(Long id) {
        Recall recall = recallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recall not found"));
        return RecallResponse.from(recall);
    }

    @Transactional(readOnly = true)
    public Page<RecallResponse> getRecallsByStatus(Recall.RecallStatus status, Pageable pageable) {
        return recallRepository.findByStatus(status, pageable)
                .map(RecallResponse::from);
    }

    @Transactional(readOnly = true)
    public List<RecallResponse> getActiveRecalls() {
        return recallRepository.findByStatus(Recall.RecallStatus.ACTIVE, Pageable.unpaged())
                .map(RecallResponse::from)
                .toList();
    }
}
