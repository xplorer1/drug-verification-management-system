package com.pharma.drugverification.dto;

import com.pharma.drugverification.domain.Recall;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RecallRequest {

    @NotNull(message = "Batch ID is required")
    private Long batchId;

    @NotNull(message = "Classification is required")
    private Recall.RecallClassification classification;

    @NotBlank(message = "Reason is required")
    private String reason;

    private String instructions;
}
