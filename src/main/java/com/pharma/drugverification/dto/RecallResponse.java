package com.pharma.drugverification.dto;

import com.pharma.drugverification.domain.Recall;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecallResponse {

    private Long id;
    private Long batchId;
    private String batchNumber;
    private Recall.RecallClassification classification;
    private Recall.RecallStatus status;
    private String reason;
    private String instructions;
    private Integer totalUnitsAffected;
    private Integer unitsRecovered;
    private Double effectivenessPercentage;
    private LocalDateTime initiatedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;

    public static RecallResponse from(Recall recall) {
        RecallResponse response = new RecallResponse();
        response.setId(recall.getId());
        response.setBatchId(recall.getBatchId());
        response.setBatchNumber(recall.getBatch() != null ? recall.getBatch().getBatchNumber() : null);
        response.setClassification(recall.getClassification());
        response.setStatus(recall.getStatus());
        response.setReason(recall.getReason());
        response.setInstructions(null); // Not in Recall entity
        response.setTotalUnitsAffected(recall.getAffectedUnits());
        response.setUnitsRecovered(recall.getRecoveredUnits());
        response.setEffectivenessPercentage(recall.getEffectiveness());
        response.setInitiatedAt(recall.getCreatedAt());
        response.setCompletedAt(recall.getClosedAt());
        response.setCreatedAt(recall.getCreatedAt());
        return response;
    }
}
