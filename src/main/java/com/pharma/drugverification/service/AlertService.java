package com.pharma.drugverification.service;

import com.pharma.drugverification.domain.Alert;
import com.pharma.drugverification.dto.AlertResolutionRequest;
import com.pharma.drugverification.dto.AlertResponse;
import com.pharma.drugverification.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pharma.drugverification.exception.BadRequestException;
import com.pharma.drugverification.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final AlertRepository alertRepository;
    private final AuditService auditService;

    @Transactional
    public Alert createAlert(String alertType, String severity, String message, String entityType, Long entityId) {
        Alert alert = new Alert();
        alert.setAlertType(Alert.AlertType.valueOf(alertType));
        alert.setSeverity(Alert.AlertSeverity.valueOf(severity.toUpperCase()));
        alert.setMessage(message);
        alert.setRelatedEntityType(entityType);
        alert.setRelatedEntityId(entityId);
        alert.setAcknowledged(false);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("timestamp", System.currentTimeMillis());
        alert.setMetadata(metadata);

        Alert saved = alertRepository.save(alert);
        log.warn("Alert created: {} [{}] - {}", alertType, severity, message);

        return saved;
    }

    @Transactional
    public AlertResponse acknowledgeAlert(Long alertId, Long userId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        if (alert.getAcknowledged()) {
            throw new BadRequestException("Alert is already acknowledged");
        }

        alert.setAcknowledged(true);
        alert.setAcknowledgedByUserId(userId);
        alert.setAcknowledgedAt(LocalDateTime.now());

        Alert saved = alertRepository.save(alert);

        auditService.log("ALERT_ACKNOWLEDGED", "Alert", alertId, userId,
                Map.of("type", alert.getAlertType().name(), "severity", alert.getSeverity().name()));

        log.info("Alert {} acknowledged by user {}", alertId, userId);

        return AlertResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public AlertResponse getAlert(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));
        return AlertResponse.from(alert);
    }

    @Transactional(readOnly = true)
    public Page<AlertResponse> getAllAlerts(Pageable pageable) {
        return alertRepository.findAll(pageable)
                .map(AlertResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<AlertResponse> getAlertsBySeverity(Alert.AlertSeverity severity, Pageable pageable) {
        return alertRepository.findBySeverity(severity, pageable)
                .map(AlertResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<AlertResponse> getAlertsByEntity(String entityType, Long entityId, Pageable pageable) {
        return alertRepository.findByRelatedEntityTypeAndRelatedEntityId(entityType, entityId, pageable)
                .map(AlertResponse::from);
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> getActiveAlerts() {
        return alertRepository.findByAcknowledged(false)
                .stream()
                .map(AlertResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> getUnacknowledgedAlerts() {
        return alertRepository.findByAcknowledged(false)
                .stream()
                .map(AlertResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAlertStatistics(LocalDateTime since) {
        List<Alert> alerts = alertRepository.findByCreatedAtAfter(since);

        long total = alerts.size();
        long acknowledged = alerts.stream().filter(Alert::getAcknowledged).count();
        long unacknowledged = total - acknowledged;

        Map<String, Long> bySeverity = new HashMap<>();
        for (Alert.AlertSeverity sev : Alert.AlertSeverity.values()) {
            bySeverity.put(sev.name(), alerts.stream().filter(a -> a.getSeverity() == sev).count());
        }

        Map<String, Long> byType = new HashMap<>();
        for (Alert.AlertType type : Alert.AlertType.values()) {
            byType.put(type.name(), alerts.stream().filter(a -> a.getAlertType() == type).count());
        }

        return Map.of(
                "total", total,
                "acknowledged", acknowledged,
                "unacknowledged", unacknowledged,
                "bySeverity", bySeverity,
                "byType", byType);
    }
}
