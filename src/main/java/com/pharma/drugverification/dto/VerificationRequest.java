package com.pharma.drugverification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerificationRequest {

    @NotBlank(message = "Serial number is required")
    private String serialNumber;

    private Double latitude;

    private Double longitude;

    private String location;

    private String deviceId;
}
