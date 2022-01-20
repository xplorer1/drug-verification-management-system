package com.pharma.drugverification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BatchCreationRequest {

    @NotBlank(message = "Batch number is required")
    @Size(min = 3, max = 100, message = "Batch number must be between 3 and 100 characters")
    private String batchNumber;

    @NotNull(message = "Drug ID is required")
    private Long drugId;

    @NotNull(message = "Manufacturing date is required")
    private LocalDate manufacturingDate;

    @NotNull(message = "Expiration date is required")
    private LocalDate expirationDate;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    private String location;

    private String notes;
}
