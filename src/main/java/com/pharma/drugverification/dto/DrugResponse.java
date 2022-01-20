package com.pharma.drugverification.dto;

import com.pharma.drugverification.domain.Drug;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing drug details")
public class DrugResponse {

    @Schema(description = "Drug internal ID", example = "1")
    private Long id;
    @Schema(description = "Commercial name of the drug", example = "Aspirin")
    private String name;
    @Schema(description = "Cryptographic unique identifier for the drug type", example = "DRUG-X-YZ")
    private String cryptoIdentifier;
    @Schema(description = "National Drug Code", example = "12345-678-90")
    private String ndc;
    @Schema(description = "Manufacturer name", example = "PharmaCorp")
    private String manufacturer;
    @Schema(description = "Manufacturer internal ID", example = "1001")
    private Long manufacturerId;
    @Schema(description = "Detailed drug description")
    private String description;
    @Schema(description = "Form (e.g., Tablet, Injection)")
    private String dosageForm;
    @Schema(description = "Dosage strength", example = "500mg")
    private String strength;
    @Schema(description = "Current registration status")
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
