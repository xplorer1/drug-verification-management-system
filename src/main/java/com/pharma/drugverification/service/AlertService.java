package com.pharma.drugverification.service;

import com.pharma.drugverification.domain.Alert;
import com.pharma.drugverification.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final AlertRepository alertRepository;

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
        log.warn("Alert created: {} - {}", alertType, message);

        return saved;
    }

    @Transactional
    public void acknowledgeAlert(Long alertId, Long userId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        alert.setAcknowledged(true);
        alert.setAcknowledgedByUserId(userId);
        alert.setAcknowledgedAt(java.time.LocalDateTime.now());

        alertRepository.save(alert);
        log.info("Alert {} acknowledged by user {}", alertId, userId);
    }
}
