package com.pharma.drugverification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DrugRegistrationRequest {

    @NotBlank(message = "Drug name is required")
    @Size(min = 2, max = 255, message = "Drug name must be between 2 and 255 characters")
    private String name;

    @NotBlank(message = "NDC is required")
    @Size(min = 3, max = 50, message = "NDC must be between 3 and 50 characters")
    private String ndc;

    @NotBlank(message = "Manufacturer name is required")
    @Size(min = 2, max = 255, message = "Manufacturer name must be between 2 and 255 characters")
    private String manufacturer;

    @NotNull(message = "Manufacturer ID is required")
    private Long manufacturerId;

    private String description;

    private String dosageForm;

    private String strength;

    private BigDecimal minTemperature;

    private BigDecimal maxTemperature;
}
