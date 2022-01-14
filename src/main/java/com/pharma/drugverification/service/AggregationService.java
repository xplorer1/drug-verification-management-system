package com.pharma.drugverification.service;

import com.pharma.drugverification.domain.Aggregation;
import com.pharma.drugverification.domain.SerializedUnit;
import com.pharma.drugverification.dto.AggregationRequest;
import com.pharma.drugverification.dto.AggregationResponse;
import com.pharma.drugverification.repository.AggregationRepository;
import com.pharma.drugverification.repository.SerializedUnitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AggregationService {

    private final AggregationRepository aggregationRepository;
    private final SerializedUnitRepository serializedUnitRepository;
    private final AuditService auditService;

    @Transactional
    public AggregationResponse createAggregation(AggregationRequest request, Long userId) {
        // Validate all child units exist and are in ACTIVE status
        List<SerializedUnit> childUnits = serializedUnitRepository.findAllById(request.getChildUnitIds());

        if (childUnits.isEmpty()) {
            throw new RuntimeException("No child units found");
        }

        if (childUnits.size() != request.getChildUnitIds().size()) {
            throw new RuntimeException("Some child units not found");
        }

        for (SerializedUnit unit : childUnits) {
            if (unit.getStatus() != SerializedUnit.UnitStatus.ACTIVE) {
                throw new RuntimeException("Unit " + unit.getSerialNumber() + " is not in ACTIVE status");
            }
        }

        // Since actual entity structure uses one parent-child pair per record,
        // we need to create a parent unit if using serial number approach
        // For now, create aggregation records for each child
        List<Aggregation> aggregations = new ArrayList<>();

        for (SerializedUnit child : childUnits) {
            Aggregation aggregation = new Aggregation();
            aggregation.setParentId(Long.parseLong(request.getParentSerialNumber())); // Simplified - in production
                                                                                      // would lookup parent
            aggregation.setParentType(request.getType());
            aggregation.setChildId(child.getId());
            aggregation.setChildType(Aggregation.AggregationType.UNIT);
            aggregation.setBatchId(child.getBatchId());
            aggregation.setActive(true);

            aggregations.add(aggregationRepository.save(aggregation));

            // Update child unit
            child.setParentAggregationId(aggregation.getId());
            serializedUnitRepository.save(child);
        }

        auditService.log("AGGREGATION_CREATED", "Aggregation", aggregations.get(0).getId(), userId,
                Map.of("type", request.getType().name(), "childCount", childUnits.size()));

        log.info("Created {} aggregation with {} units", request.getType(), childUnits.size());

        return AggregationResponse.from(aggregations.get(0));
    }

    @Transactional
    public AggregationResponse disaggregate(Long aggregationId, Long userId) {
        Aggregation aggregation = aggregationRepository.findById(aggregationId)
                .orElseThrow(() -> new RuntimeException("Aggregation not found"));

        if (!aggregation.getActive()) {
            throw new RuntimeException("Aggregation is already disaggregated");
        }

        aggregation.setActive(false);
        aggregation.setDisaggregatedAt(LocalDateTime.now());
        aggregation.setDisaggregatedByUserId(userId);

        // Remove aggregation reference from child unit
        SerializedUnit child = serializedUnitRepository.findById(aggregation.getChildId()).orElse(null);
        if (child != null) {
            child.setParentAggregationId(null);
            serializedUnitRepository.save(child);
        }

        Aggregation saved = aggregationRepository.save(aggregation);

        auditService.log("AGGREGATION_DISAGGREGATED", "Aggregation", aggregationId, userId,
                Map.of("type", aggregation.getParentType().name()));

        log.info("Disaggregated {} aggregation", aggregation.getParentType());

        return AggregationResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public AggregationResponse getAggregation(Long id) {
        Aggregation aggregation = aggregationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aggregation not found"));
        return AggregationResponse.from(aggregation);
    }

    @Transactional(readOnly = true)
    public List<AggregationResponse> getAggregationsByType(Aggregation.AggregationType type) {
        // Simplified for now - would need custom repository method
        return new ArrayList<>();
    }
}
