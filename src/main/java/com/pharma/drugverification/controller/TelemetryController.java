package com.pharma.drugverification.controller;

import com.pharma.drugverification.dto.TelemetryReadingRequest;
import com.pharma.drugverification.dto.TelemetryReadingResponse;
import com.pharma.drugverification.service.TelemetryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/telemetry")
@RequiredArgsConstructor
public class TelemetryController {

    private final TelemetryService telemetryService;

    @PostMapping("/readings")
    @PreAuthorize("hasAnyRole('MANUFACTURER', 'DISTRIBUTOR', 'IOT_DEVICE', 'ADMIN')")
    public ResponseEntity<TelemetryReadingResponse> recordReading(
            @Valid @RequestBody TelemetryReadingRequest request,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        TelemetryReadingResponse response = telemetryService.recordReading(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/batch/{batchId}")
    public ResponseEntity<Page<TelemetryReadingResponse>> getReadingsByBatch(
            @PathVariable Long batchId,
            Pageable pageable) {
        Page<TelemetryReadingResponse> response = telemetryService.getReadingsByBatch(batchId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/device/{deviceId}")
    public ResponseEntity<Page<TelemetryReadingResponse>> getReadingsByDevice(
            @PathVariable String deviceId,
            Pageable pageable) {
        Page<TelemetryReadingResponse> response = telemetryService.getReadingsByDevice(deviceId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/batch/{batchId}/violations")
    public ResponseEntity<List<TelemetryReadingResponse>> getViolationsByBatch(
            @PathVariable Long batchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        LocalDateTime queryTime = since != null ? since : LocalDateTime.now().minusDays(7);
        List<TelemetryReadingResponse> response = telemetryService.getViolationsByBatch(batchId, queryTime);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/batch/{batchId}/stats")
    public ResponseEntity<Map<String, Object>> getTelemetryStats(
            @PathVariable Long batchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        LocalDateTime queryTime = since != null ? since : LocalDateTime.now().minusDays(7);
        Map<String, Object> stats = telemetryService.getTelemetryStats(batchId, queryTime);
        return ResponseEntity.ok(stats);
    }
}
