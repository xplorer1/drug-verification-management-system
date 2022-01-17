package com.pharma.drugverification.service;

import com.pharma.drugverification.domain.Aggregation;
import com.pharma.drugverification.domain.SerializedUnit;
import com.pharma.drugverification.dto.AggregationRequest;
import com.pharma.drugverification.dto.AggregationResponse;
import com.pharma.drugverification.repository.AggregationRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AggregationServiceTest {

    @Mock
    private AggregationRepository aggregationRepository;

    @Mock
    private SerializedUnitRepository serializedUnitRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AggregationService aggregationService;

    private AggregationRequest request;
    private SerializedUnit childUnit;
    private Aggregation aggregation;

    @BeforeEach
    void setUp() {
        request = new AggregationRequest();
        request.setType(Aggregation.AggregationType.CASE);
        request.setParentSerialNumber("12345");
        request.setChildUnitIds(List.of(1L));

        childUnit = new SerializedUnit();
        childUnit.setId(1L);
        childUnit.setSerialNumber("CHILD-001");
        childUnit.setStatus(SerializedUnit.UnitStatus.ACTIVE);
        childUnit.setBatchId(100L);

        aggregation = new Aggregation();
        aggregation.setId(1L);
        aggregation.setParentId(12345L);
        aggregation.setParentType(Aggregation.AggregationType.CASE);
        aggregation.setChildId(1L);
        aggregation.setChildType(Aggregation.AggregationType.UNIT);
        aggregation.setBatchId(100L);
        aggregation.setActive(true);
        aggregation.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createAggregation_Success() {
        when(serializedUnitRepository.findAllById(any())).thenReturn(List.of(childUnit));
        when(aggregationRepository.save(any(Aggregation.class))).thenReturn(aggregation);

        AggregationResponse response = aggregationService.createAggregation(request, 1L);

        assertNotNull(response);
        assertEquals(Aggregation.AggregationType.CASE, response.getType());
        assertEquals("12345", response.getParentSerialNumber());
        verify(auditService, times(1)).log(eq("AGGREGATION_CREATED"), eq("Aggregation"), any(), eq(1L), any());
    }

    @Test
    void disaggregate_Success() {
        when(aggregationRepository.findById(1L)).thenReturn(Optional.of(aggregation));
        when(serializedUnitRepository.findById(1L)).thenReturn(Optional.of(childUnit));
        when(aggregationRepository.save(any(Aggregation.class))).thenReturn(aggregation);

        AggregationResponse response = aggregationService.disaggregate(1L, 1L);

        assertNotNull(response);
        verify(auditService, times(1)).log(eq("AGGREGATION_DISAGGREGATED"), eq("Aggregation"), eq(1L), eq(1L), any());
    }
}
