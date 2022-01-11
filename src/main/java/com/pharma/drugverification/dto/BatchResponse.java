package com.pharma.drugverification.dto;

import com.pharma.drugverification.domain.Batch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchResponse {

    private Long id;
    private String batchNumber;
    private Long drugId;
    private String drugName;
    private LocalDate manufacturingDate;
    private LocalDate expirationDate;
    private Integer quantity;
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
