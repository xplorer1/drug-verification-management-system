package com.pharma.drugverification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TelemetryReadingRequest {

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotNull(message = "Batch ID is required")
    private Long batchId;

    @NotNull(message = "Temperature is required")
    private Double temperatureCelsius;

    @NotNull(message = "Humidity is required")
    private Double humidityPercent;

    private String location;
}
