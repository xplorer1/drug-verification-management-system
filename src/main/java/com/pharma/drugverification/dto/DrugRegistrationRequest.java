package com.pharma.drugverification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DrugRegistrationRequest {

    @NotBlank(message = "Drug name is required")
    private String name;

    @NotBlank(message = "NDC is required")
    private String ndc;

    @NotBlank(message = "Manufacturer name is required")
    private String manufacturer;

    @NotNull(message = "Manufacturer ID is required")
    private Long manufacturerId;

    private String description;

    private String dosageForm;

    private String strength;

    private BigDecimal minTemperature;

    private BigDecimal maxTemperature;
}
