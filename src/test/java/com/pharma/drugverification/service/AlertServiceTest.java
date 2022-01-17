package com.pharma.drugverification.service;

import com.pharma.drugverification.domain.Alert;
import com.pharma.drugverification.dto.AlertResolutionRequest;
import com.pharma.drugverification.dto.AlertResponse;
import com.pharma.drugverification.repository.AlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AlertService alertService;

    private Alert alert;

    @BeforeEach
    void setUp() {
        alert = new Alert();
        alert.setId(1L);
        alert.setAlertType(Alert.AlertType.TEMPERATURE_EXCURSION);
        alert.setSeverity(Alert.AlertSeverity.HIGH);
        alert.setMessage("Temp too high");
        alert.setRelatedEntityType("Batch");
        alert.setRelatedEntityId(100L);
        alert.setAcknowledged(false);
        alert.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createAlert_Success() {
        when(alertRepository.save(any(Alert.class))).thenReturn(alert);

        Alert result = alertService.createAlert("TEMPERATURE_EXCURSION", "HIGH", "Temp too high", "Batch", 100L);

        assertNotNull(result);
        assertEquals(Alert.AlertType.TEMPERATURE_EXCURSION, result.getAlertType());
        verify(alertRepository, times(1)).save(any(Alert.class));
    }

    @Test
    void acknowledgeAlert_Success() {
        when(alertRepository.findById(1L)).thenReturn(Optional.of(alert));
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> {
            Alert a = invocation.getArgument(0);
            return a; // return the modified alert
        });

        AlertResponse response = alertService.acknowledgeAlert(1L, 500L);

        assertNotNull(response);
        assertTrue(response.getAcknowledged());
        assertEquals(500L, response.getAcknowledgedByUserId());
        verify(auditService, times(1)).log(eq("ALERT_ACKNOWLEDGED"), eq("Alert"), eq(1L), eq(500L), any());
    }

    @Test
    void getActiveAlerts_Success() {
        when(alertRepository.findByAcknowledged(false)).thenReturn(List.of(alert));

        List<AlertResponse> alerts = alertService.getActiveAlerts();

        assertNotNull(alerts);
        assertEquals(1, alerts.size());
        assertFalse(alerts.get(0).getAcknowledged());
    }
}
