package com.pharma.drugverification.controller;

import com.pharma.drugverification.domain.Alert;
import com.pharma.drugverification.dto.AlertResponse;
import com.pharma.drugverification.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULATOR', 'MANUFACTURER')")
    public ResponseEntity<Page<AlertResponse>> getAllAlerts(Pageable pageable) {
        Page<AlertResponse> response = alertService.getAllAlerts(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULATOR', 'MANUFACTURER', 'PHARMACIST')")
    public ResponseEntity<AlertResponse> getAlert(@PathVariable Long id) {
        AlertResponse response = alertService.getAlert(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/severity/{severity}")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULATOR', 'MANUFACTURER')")
    public ResponseEntity<Page<AlertResponse>> getAlertsBySeverity(
            @PathVariable Alert.AlertSeverity severity,
            Pageable pageable) {
        Page<AlertResponse> response = alertService.getAlertsBySeverity(severity, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<Page<AlertResponse>> getAlertsByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            Pageable pageable) {
        Page<AlertResponse> response = alertService.getAlertsByEntity(entityType, entityId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULATOR', 'MANUFACTURER')")
    public ResponseEntity<List<AlertResponse>> getActiveAlerts() {
        List<AlertResponse> response = alertService.getActiveAlerts();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unacknowledged")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULATOR', 'MANUFACTURER')")
    public ResponseEntity<List<AlertResponse>> getUnacknowledgedAlerts() {
        List<AlertResponse> response = alertService.getUnacknowledgedAlerts();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/acknowledge")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULATOR', 'MANUFACTURER', 'PHARMACIST')")
    public ResponseEntity<AlertResponse> acknowledgeAlert(
            @PathVariable Long id,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        AlertResponse response = alertService.acknowledgeAlert(id, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULATOR')")
    public ResponseEntity<Map<String, Object>> getAlertStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        LocalDateTime queryTime = since != null ? since : LocalDateTime.now().minusDays(30);
        Map<String, Object> stats = alertService.getAlertStatistics(queryTime);
        return ResponseEntity.ok(stats);
    }
}
