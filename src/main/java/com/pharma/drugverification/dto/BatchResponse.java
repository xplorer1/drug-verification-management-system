package com.pharma.drugverification.dto;

import com.pharma.drugverification.domain.Batch;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing batch details and its current status")
public class BatchResponse {

    @Schema(description = "Batch internal ID", example = "1")
    private Long id;
    @Schema(description = "Unique batch identification number", example = "BN-2024-001")
    private String batchNumber;
    @Schema(description = "ID of the drug this batch belongs to", example = "10")
    private Long drugId;
    @Schema(description = "Name of the drug", example = "Aspirin")
    private String drugName;
    @Schema(description = "Date when the batch was manufactured")
    private LocalDate manufacturingDate;
    @Schema(description = "Date when the batch expires")
    private LocalDate expirationDate;
    @Schema(description = "Total quantity of units in the batch", example = "1000")
    private Integer quantity;
    @Schema(description = "Current lifecycle status of the batch")
    private Batch.BatchStatus status;
    private String location;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BatchResponse from(Batch batch) {
        BatchResponse response = new BatchResponse();
        response.setId(batch.getId());
        response.setBatchNumber(batch.getBatchNumber());
        response.setDrugId(batch.getDrugId());
        response.setDrugName(batch.getDrug() != null ? batch.getDrug().getName() : null);
        response.setManufacturingDate(batch.getManufacturingDate());
        response.setExpirationDate(batch.getExpirationDate());
        response.setQuantity(batch.getQuantity());
        response.setStatus(batch.getStatus());
        response.setLocation(batch.getLocation());
        response.setNotes(batch.getNotes());
        response.setCreatedAt(batch.getCreatedAt());
        response.setUpdatedAt(batch.getUpdatedAt());
        return response;
    }
}
