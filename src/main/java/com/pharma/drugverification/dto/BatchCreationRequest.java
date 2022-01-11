package com.pharma.drugverification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BatchCreationRequest {

    @NotBlank(message = "Batch number is required")
    private String batchNumber;

    @NotNull(message = "Drug ID is required")
    private Long drugId;

    @NotNull(message = "Manufacturing date is required")
    private LocalDate manufacturingDate;

    @NotNull(message = "Expiration date is required")
    private LocalDate expirationDate;

    @NotNull(message = "Quantity is required")
    private Integer quantity;

    private String location;

    private String notes;
}
