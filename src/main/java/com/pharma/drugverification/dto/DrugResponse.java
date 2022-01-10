package com.pharma.drugverification.dto;

import com.pharma.drugverification.domain.Drug;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrugResponse {

    private Long id;
    private String name;
    private String cryptoIdentifier;
    private String ndc;
    private String manufacturer;
    private Long manufacturerId;
    private String description;
    private String dosageForm;
    private String strength;
    private Drug.DrugStatus status;
    private BigDecimal minTemperature;
    private BigDecimal maxTemperature;
    private String rejectionReason;
    private Long regulatorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime approvedAt;

    public static DrugResponse from(Drug drug) {
        DrugResponse response = new DrugResponse();
        response.setId(drug.getId());
        response.setName(drug.getName());
        response.setCryptoIdentifier(drug.getCryptoIdentifier());
        response.setNdc(drug.getNdc());
        response.setManufacturer(drug.getManufacturer());
        response.setManufacturerId(drug.getManufacturerId());
        response.setDescription(drug.getDescription());
        response.setDosageForm(drug.getDosageForm());
        response.setStrength(drug.getStrength());
        response.setStatus(drug.getStatus());
        response.setMinTemperature(drug.getMinTemperature());
        response.setMaxTemperature(drug.getMaxTemperature());
        response.setRejectionReason(drug.getRejectionReason());
        response.setRegulatorId(drug.getRegulatorId());
        response.setCreatedAt(drug.getCreatedAt());
        response.setUpdatedAt(drug.getUpdatedAt());
        response.setApprovedAt(drug.getApprovedAt());
        return response;
    }
}
