package com.pharma.drugverification.service;

import com.pharma.drugverification.domain.Batch;
import com.pharma.drugverification.domain.Drug;
import com.pharma.drugverification.domain.TelemetryReading;
import com.pharma.drugverification.dto.TelemetryReadingRequest;
import com.pharma.drugverification.dto.TelemetryReadingResponse;
import com.pharma.drugverification.repository.BatchRepository;
import com.pharma.drugverification.repository.TelemetryReadingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelemetryService {

        private final TelemetryReadingRepository telemetryReadingRepository;
        private final BatchRepository batchRepository;
        private final AlertService alertService;
        private final AuditService auditService;

        @Transactional
        public TelemetryReadingResponse recordReading(TelemetryReadingRequest request, Long userId) {
                Batch batch = batchRepository.findById(request.getBatchId())
                                .orElseThrow(() -> new RuntimeException("Batch not found"));

                Drug drug = batch.getDrug();
                if (drug == null) {
                        throw new RuntimeException("Drug information not found for batch");
                }

                // Create telemetry reading
                TelemetryReading reading = new TelemetryReading();
                reading.setDeviceId(request.getDeviceId());
                reading.setBatchId(request.getBatchId());
                reading.setTemperature(BigDecimal.valueOf(request.getTemperatureCelsius()));
                reading.setHumidity(BigDecimal.valueOf(request.getHumidityPercent()));
                reading.setLocation(request.getLocation());
                reading.setTimestamp(LocalDateTime.now());

                TelemetryReading saved = telemetryReadingRepository.save(reading);

                // Check for threshold violations
                boolean violation = false;
                StringBuilder violationMessage = new StringBuilder();

                if (drug.getMinTemperature() != null && drug.getMaxTemperature() != null &&
                                (request.getTemperatureCelsius() < drug.getMinTemperature().doubleValue() ||
                                                request.getTemperatureCelsius() > drug.getMaxTemperature()
                                                                .doubleValue())) {
                        violation = true;
                        violationMessage.append(String.format(
                                        "Temperature %.1f°C outside range [%.1f, %.1f]. ",
                                        request.getTemperatureCelsius(),
                                        drug.getMinTemperature().doubleValue(),
                                        drug.getMaxTemperature().doubleValue()));
                }

                // Create alert if threshold violated
                if (violation) {
                        alertService.createAlert(
                                        "TEMPERATURE_EXCURSION",
                                        "High",
                                        String.format("Batch %s: %s", batch.getBatchNumber(),
                                                        violationMessage.toString()),
                                        "Batch",
                                        batch.getId());

                        auditService.log("TEMPERATURE_EXCURSION", "Batch", batch.getId(), userId,
                                        Map.of("deviceId", request.getDeviceId(),
                                                        "temperature", request.getTemperatureCelsius(),
                                                        "location",
                                                        request.getLocation() != null ? request.getLocation() : ""));

                        log.warn("Temperature excursion detected: Batch {} - {}",
                                        batch.getBatchNumber(), violationMessage.toString());
                }

                TelemetryReadingResponse response = TelemetryReadingResponse.from(saved);
                response.setThresholdViolation(violation);
                return response;
        }

        @Transactional(readOnly = true)
        public Page<TelemetryReadingResponse> getReadingsByBatch(Long batchId, Pageable pageable) {
                return telemetryReadingRepository.findByBatchId(batchId, pageable)
                                .map(TelemetryReadingResponse::from);
        }

        @Transactional(readOnly = true)
        public Page<TelemetryReadingResponse> getReadingsByDevice(String deviceId, Pageable pageable) {
                return telemetryReadingRepository.findByDeviceId(deviceId, pageable)
                                .map(TelemetryReadingResponse::from);
        }

        @Transactional(readOnly = true)
        public List<TelemetryReadingResponse> getViolationsByBatch(Long batchId, LocalDateTime since) {
                // Get all readings and filter in memory for violations
                List<TelemetryReading> readings = telemetryReadingRepository
                                .findByBatchIdOrderByTimestampDesc(batchId);

                Batch batch = batchRepository.findById(batchId).orElse(null);
                if (batch == null || batch.getDrug() == null) {
                        return List.of();
                }

                Drug drug = batch.getDrug();

                return readings.stream()
                                .filter(r -> r.getTemperature() != null && drug.getMinTemperature() != null &&
                                                drug.getMaxTemperature() != null &&
                                                (r.getTemperature()
                                                                .doubleValue() < drug.getMinTemperature().doubleValue()
                                                                ||
                                                                r.getTemperature().doubleValue() > drug
                                                                                .getMaxTemperature().doubleValue()))
                                .map(r -> {
                                        TelemetryReadingResponse resp = TelemetryReadingResponse.from(r);
                                        resp.setThresholdViolation(true);
                                        return resp;
                                })
                                .toList();
        }

        @Transactional(readOnly = true)
        public Map<String, Object> getTelemetryStats(Long batchId, LocalDateTime since) {
                List<TelemetryReading> readings = telemetryReadingRepository
                                .findByBatchIdOrderByTimestampDesc(batchId);

                if (readings.isEmpty()) {
                        return Map.of(
                                        "totalReadings", 0,
                                        "violations", 0,
                                        "averageTemperature", 0.0,
                                        "minTemperature", 0.0,
                                        "maxTemperature", 0.0,
                                        "averageHumidity", 0.0);
                }

                Batch batch = batchRepository.findById(batchId).orElse(null);
                long violations = 0;

                if (batch != null && batch.getDrug() != null) {
                        Drug drug = batch.getDrug();
                        violations = readings.stream()
                                        .filter(r -> r.getTemperature() != null && drug.getMinTemperature() != null &&
                                                        drug.getMaxTemperature() != null &&
                                                        (r.getTemperature().doubleValue() < drug.getMinTemperature()
                                                                        .doubleValue() ||
                                                                        r.getTemperature().doubleValue() > drug
                                                                                        .getMaxTemperature()
                                                                                        .doubleValue()))
                                        .count();
                }

                double avgTemp = readings.stream()
                                .filter(r -> r.getTemperature() != null)
                                .mapToDouble(r -> r.getTemperature().doubleValue())
                                .average()
                                .orElse(0.0);

                double minTemp = readings.stream()
                                .filter(r -> r.getTemperature() != null)
                                .mapToDouble(r -> r.getTemperature().doubleValue())
                                .min()
                                .orElse(0.0);

                double maxTemp = readings.stream()
                                .filter(r -> r.getTemperature() != null)
                                .mapToDouble(r -> r.getTemperature().doubleValue())
                                .max()
                                .orElse(0.0);

                double avgHumidity = readings.stream()
                                .filter(r -> r.getHumidity() != null)
                                .mapToDouble(r -> r.getHumidity().doubleValue())
                                .average()
                                .orElse(0.0);

                return Map.of(
                                "totalReadings", readings.size(),
                                "violations", violations,
                                "averageTemperature", String.format("%.1f", avgTemp),
                                "minTemperature", String.format("%.1f", minTemp),
                                "maxTemperature", String.format("%.1f", maxTemp),
                                "averageHumidity", String.format("%.1f", avgHumidity));
        }
}
