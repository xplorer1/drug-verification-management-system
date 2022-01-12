package com.pharma.drugverification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SerializedUnitCreationRequest {

    @NotBlank(message = "Serial number is required")
    private String serialNumber;

    @NotNull(message = "Batch ID is required")
    private Long batchId;

    @NotBlank(message = "GTIN is required")
    private String gtin;

    private Long parentAggregationId;
}
