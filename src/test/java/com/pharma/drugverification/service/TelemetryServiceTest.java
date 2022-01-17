package com.pharma.drugverification.service;

import com.pharma.drugverification.domain.Batch;
import com.pharma.drugverification.domain.Drug;
import com.pharma.drugverification.domain.TelemetryReading;
import com.pharma.drugverification.dto.TelemetryReadingRequest;
import com.pharma.drugverification.dto.TelemetryReadingResponse;
import com.pharma.drugverification.repository.BatchRepository;
import com.pharma.drugverification.repository.TelemetryReadingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelemetryServiceTest {

    @Mock
    private TelemetryReadingRepository telemetryReadingRepository;

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private AlertService alertService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private TelemetryService telemetryService;

    private TelemetryReadingRequest request;
    private Batch batch;
    private Drug drug;
    private TelemetryReading reading;

    @BeforeEach
    void setUp() {
        request = new TelemetryReadingRequest();
        request.setDeviceId("SENSOR-001");
        request.setBatchId(1L);
        request.setTemperatureCelsius(25.0);
        request.setHumidityPercent(60.0);
        request.setLocation("Warehouse A");

        drug = new Drug();
        drug.setId(1L);
        drug.setName("ValidDrug");
        drug.setMinTemperature(BigDecimal.valueOf(2.0));
        drug.setMaxTemperature(BigDecimal.valueOf(8.0)); // 2-8 degrees

        batch = new Batch();
        batch.setId(1L);
        batch.setBatchNumber("BATCH-123");
        batch.setDrug(drug);

        reading = new TelemetryReading();
        reading.setId(1L);
        reading.setDeviceId("SENSOR-001");
        reading.setBatchId(1L);
        reading.setTemperature(BigDecimal.valueOf(25.0));
        reading.setHumidity(BigDecimal.valueOf(60.0));
        reading.setTimestamp(LocalDateTime.now());
    }

    @Test
    void recordReading_Violation() {
        when(batchRepository.findById(1L)).thenReturn(Optional.of(batch));
        when(telemetryReadingRepository.save(any(TelemetryReading.class))).thenReturn(reading);

        TelemetryReadingResponse response = telemetryService.recordReading(request, 1L);

        assertNotNull(response);
        assertTrue(response.getThresholdViolation()); // 25.0 is > 8.0
        verify(alertService, times(1)).createAlert(eq("TEMPERATURE_EXCURSION"), eq("High"), any(), eq("Batch"), eq(1L));
        verify(auditService, times(1)).log(eq("TEMPERATURE_EXCURSION"), eq("Batch"), eq(1L), eq(1L), any());
    }

    @Test
    void recordReading_NoViolation() {
        request.setTemperatureCelsius(5.0); // Within 2-8 range
        reading.setTemperature(BigDecimal.valueOf(5.0));

        when(batchRepository.findById(1L)).thenReturn(Optional.of(batch));
        when(telemetryReadingRepository.save(any(TelemetryReading.class))).thenReturn(reading);

        TelemetryReadingResponse response = telemetryService.recordReading(request, 1L);

        assertNotNull(response);
        assertFalse(response.getThresholdViolation());
        verify(alertService, never()).createAlert(any(), any(), any(), any(), any());
    }
}
