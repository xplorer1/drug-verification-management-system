package com.pharma.drugverification.dto;

import com.pharma.drugverification.domain.TelemetryReading;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryReadingResponse {

    private Long id;
    private String deviceId;
    private Long batchId;
    private Double temperatureCelsius;
    private Double humidityPercent;
    private String location;
    private Boolean thresholdViolation;
    private LocalDateTime timestamp;

    public static TelemetryReadingResponse from(TelemetryReading reading) {
        TelemetryReadingResponse response = new TelemetryReadingResponse();
        response.setId(reading.getId());
        response.setDeviceId(reading.getDeviceId());
        response.setBatchId(reading.getBatchId());
        response.setTemperatureCelsius(
                reading.getTemperature() != null ? reading.getTemperature().doubleValue() : null);
        response.setHumidityPercent(reading.getHumidity() != null ? reading.getHumidity().doubleValue() : null);
        response.setLocation(reading.getLocation());
        response.setThresholdViolation(false); // Calculated separately
        response.setTimestamp(reading.getTimestamp());
        return response;
    }
}
