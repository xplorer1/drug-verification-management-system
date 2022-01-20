package com.pharma.drugverification.service;

import com.pharma.drugverification.domain.Batch;
import com.pharma.drugverification.domain.Recall;
import com.pharma.drugverification.domain.Recall.RecallClassification;
import com.pharma.drugverification.domain.Recall.RecallStatus;
import com.pharma.drugverification.domain.SerializedUnit;
import com.pharma.drugverification.dto.RecallRequest;
import com.pharma.drugverification.dto.RecallResponse;
import com.pharma.drugverification.repository.BatchRepository;
import com.pharma.drugverification.repository.RecallRepository;
import com.pharma.drugverification.repository.SerializedUnitRepository;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecallServiceTest {

    @Mock
    private RecallRepository recallRepository;

    @Mock
    private SerializedUnitRepository serializedUnitRepository;

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private AlertService alertService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private RecallService recallService;

    private RecallRequest request;
    private Batch batch;
    private Recall recall;

    @BeforeEach
    void setUp() {
        request = new RecallRequest();
        request.setBatchId(1L);
        request.setInstructions("Return to manufacturer");
        request.setReason("Quality issue");
        request.setClassification(RecallClassification.CLASS_I);

        batch = new Batch();
        batch.setId(1L);
        batch.setBatchNumber("BATCH-123");

        recall = new Recall();
        recall.setId(1L);
        recall.setBatchId(1L);
        recall.setRecallNumber("RECALL-20220114-1");
        recall.setClassification(RecallClassification.CLASS_I);
        recall.setStatus(RecallStatus.ACTIVE);
        recall.setReason("Quality issue");
        recall.setAffectedUnits(10);
        recall.setRecoveredUnits(0);
        recall.setEffectiveness(0.0);
        recall.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void initiateRecall_Success() {
        // Arrange
        SerializedUnit unit1 = new SerializedUnit();
        unit1.setId(101L);
        unit1.setStatus(SerializedUnit.UnitStatus.ACTIVE);

        when(batchRepository.findById(1L)).thenReturn(Optional.of(batch));
        // Mock exists check to return false (no active recall)
        when(recallRepository.existsByBatchIdAndStatus(eq(1L), any(RecallStatus.class))).thenReturn(false);
        when(serializedUnitRepository.countByBatchId(1L)).thenReturn(1L);
        when(recallRepository.save(any(Recall.class))).thenReturn(recall);
        when(serializedUnitRepository.updateStatusByBatchIdAndStatus(anyLong(), any(), any())).thenReturn(1);

        // Act
        RecallResponse response = recallService.initiateRecall(request, 1L);

        // Assert
        assertNotNull(response);
        assertEquals(RecallStatus.ACTIVE, response.getStatus());

        // Verify bulk update called instead of individual saves
        verify(serializedUnitRepository, times(1)).updateStatusByBatchIdAndStatus(
                eq(1L),
                eq(SerializedUnit.UnitStatus.ACTIVE),
                eq(SerializedUnit.UnitStatus.QUARANTINED));
        verify(serializedUnitRepository, never()).save(any(SerializedUnit.class));

        verify(alertService, times(1)).createAlert(eq("RECALL_INITIATED"), eq("High"), any(), eq("Recall"), eq(1L));
        verify(auditService, times(1)).log(eq("RECALL_INITIATED"), eq("Recall"), eq(1L), eq(1L), any());
    }

    @Test
    void completeRecall_Success() {
        when(recallRepository.findById(1L)).thenReturn(Optional.of(recall));
        when(recallRepository.save(any(Recall.class))).thenReturn(recall);

        RecallResponse response = recallService.completeRecall(1L, 1L);

        assertNotNull(response);
        verify(recallRepository, times(1)).save(any(Recall.class));
        verify(auditService, times(1)).log(eq("RECALL_COMPLETED"), eq("Recall"), eq(1L), eq(1L), any());
    }
}
