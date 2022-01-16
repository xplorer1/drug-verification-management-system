package com.pharma.drugverification.dto;

import com.pharma.drugverification.domain.Alert;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponse {

    private Long id;
    private Alert.AlertType alertType;
    private Alert.AlertSeverity severity;
    private String message;
    private String relatedEntityType;
    private Long relatedEntityId;
    private Boolean acknowledged;
    private Long acknowledgedByUserId;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime createdAt;

    public static AlertResponse from(Alert alert) {
        AlertResponse response = new AlertResponse();
        response.setId(alert.getId());
        response.setAlertType(alert.getAlertType());
        response.setSeverity(alert.getSeverity());
        response.setMessage(alert.getMessage());
        response.setRelatedEntityType(alert.getRelatedEntityType());
        response.setRelatedEntityId(alert.getRelatedEntityId());
        response.setAcknowledged(alert.getAcknowledged());
        response.setAcknowledgedByUserId(alert.getAcknowledgedByUserId());
        response.setAcknowledgedAt(alert.getAcknowledgedAt());
        response.setCreatedAt(alert.getCreatedAt());
        return response;
    }
}
