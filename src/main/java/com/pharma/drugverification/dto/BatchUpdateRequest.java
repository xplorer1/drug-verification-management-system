package com.pharma.drugverification.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BatchUpdateRequest {

    private String location;

    private String notes;

    @NotNull(message = "Quantity is required")
    private Integer quantity;
}
