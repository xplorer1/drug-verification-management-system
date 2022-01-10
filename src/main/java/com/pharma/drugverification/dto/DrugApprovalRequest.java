package com.pharma.drugverification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DrugApprovalRequest {

    @NotBlank(message = "Reason is required for rejection")
    private String reason;
}
