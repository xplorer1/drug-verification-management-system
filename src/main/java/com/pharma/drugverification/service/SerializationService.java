package com.pharma.drugverification.service;

import com.pharma.drugverification.domain.Batch;
import com.pharma.drugverification.domain.SerializedUnit;
import com.pharma.drugverification.domain.StatusTransition;
import com.pharma.drugverification.dto.SerializedUnitCreationRequest;
import com.pharma.drugverification.dto.SerializedUnitResponse;
import com.pharma.drugverification.repository.BatchRepository;
import com.pharma.drugverification.repository.SerializedUnitRepository;
import com.pharma.drugverification.security.HsmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SerializationService {

    private final SerializedUnitRepository serializedUnitRepository;
    private final BatchRepository batchRepository;
    private final HsmService hsmService;
    private final StatusTransitionService statusTransitionService;
    private final AuditService auditService;

    @Transactional
    @CacheEvict(value = "serializedUnits", allEntries = true)
    public SerializedUnitResponse createSerializedUnit(SerializedUnitCreationRequest request, Long userId) {
        if (serializedUnitRepository.existsBySerialNumber(request.getSerialNumber())) {
            throw new RuntimeException("Unit with serial number " + request.getSerialNumber() + " already exists");
        }

        Batch batch = batchRepository.findById(request.getBatchId())
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        if (batch.getStatus() != Batch.BatchStatus.ACTIVE) {
            throw new RuntimeException("Cannot serialize units for inactive batch");
        }

        String cryptoTail = hsmService.generateCryptoTail(
                request.getSerialNumber(),
                request.getGtin(),
                batch.getBatchNumber());

        String expirationDate = batch.getExpirationDate()
                .format(DateTimeFormatter.ofPattern("yyMMdd"));

        String dataMatrix = hsmService.generateDataMatrix(
                request.getGtin(),
                request.getSerialNumber(),
                batch.getBatchNumber(),
                expirationDate);

        SerializedUnit unit = new SerializedUnit();
        unit.setSerialNumber(request.getSerialNumber());
        unit.setBatchId(request.getBatchId());
        unit.setGtin(request.getGtin());
        unit.setCryptoTail(cryptoTail);
        unit.setDataMatrix(dataMatrix);
        unit.setKeyVersion(hsmService.getCurrentKeyVersion());
        unit.setStatus(SerializedUnit.UnitStatus.ACTIVE);
        unit.setParentAggregationId(request.getParentAggregationId());

        SerializedUnit saved = serializedUnitRepository.save(unit);

        auditService.log("UNIT_SERIALIZED", "SerializedUnit", saved.getId(), userId,
                Map.of("serialNumber", request.getSerialNumber(), "batchId", request.getBatchId()));

        return SerializedUnitResponse.from(saved);
    }

    @Transactional
    @CacheEvict(value = "serializedUnits", allEntries = true)
    public List<SerializedUnitResponse> bulkCreateSerializedUnits(Long batchId, String gtin, int quantity,
            Long userId) {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        if (batch.getStatus() != Batch.BatchStatus.ACTIVE) {
            throw new RuntimeException("Cannot serialize units for inactive batch");
        }

        List<SerializedUnitResponse> responses = new ArrayList<>();

        for (int i = 0; i < quantity; i++) {
            String serialNumber = hsmService.generateSerialNumber();

            SerializedUnitCreationRequest request = new SerializedUnitCreationRequest();
            request.setSerialNumber(serialNumber);
            request.setBatchId(batchId);
            request.setGtin(gtin);

            SerializedUnitResponse response = createSerializedUnit(request, userId);
            responses.add(response);
        }

        log.info("Bulk created {} serialized units for batch {}", quantity, batch.getBatchNumber());
        return responses;
    }

    @Transactional
    @CacheEvict(value = "serializedUnits", allEntries = true)
    public SerializedUnitResponse decommissionUnit(Long unitId, Long userId, String pharmacy) {
        SerializedUnit unit = serializedUnitRepository.findById(unitId)
                .orElseThrow(() -> new RuntimeException("Serialized unit not found"));

        if (unit.getStatus() == SerializedUnit.UnitStatus.DISPENSED) {
            throw new RuntimeException("Unit is already dispensed");
        }

        if (unit.getStatus() == SerializedUnit.UnitStatus.DESTROYED) {
            throw new RuntimeException("Unit is already destroyed");
        }

        SerializedUnit.UnitStatus oldStatus = unit.getStatus();
        unit.setStatus(SerializedUnit.UnitStatus.DISPENSED);
        unit.setDispensedAt(LocalDateTime.now());
        unit.setDispensedByUserId(userId);
        unit.setDispensedByPharmacy(pharmacy);

        SerializedUnit saved = serializedUnitRepository.save(unit);

        statusTransitionService.recordTransition(
                StatusTransition.EntityType.SERIALIZED_UNIT,
                unitId,
                oldStatus.name(),
                SerializedUnit.UnitStatus.DISPENSED.name(),
                "Unit decommissioned at " + pharmacy,
                userId);

        auditService.log("UNIT_DECOMMISSIONED", "SerializedUnit", unitId, userId,
                Map.of("serialNumber", unit.getSerialNumber(), "pharmacy", pharmacy));

        return SerializedUnitResponse.from(saved);
    }

    @Transactional
    @CacheEvict(value = "serializedUnits", allEntries = true)
    public SerializedUnitResponse revertDecommission(Long unitId, String reason, Long userId) {
        SerializedUnit unit = serializedUnitRepository.findById(unitId)
                .orElseThrow(() -> new RuntimeException("Serialized unit not found"));

        if (unit.getStatus() != SerializedUnit.UnitStatus.DISPENSED) {
            throw new RuntimeException("Unit is not dispensed");
        }

        SerializedUnit.UnitStatus oldStatus = unit.getStatus();
        unit.setStatus(SerializedUnit.UnitStatus.ACTIVE);
        unit.setDispensedAt(null);
        unit.setDispensedByUserId(null);
        unit.setDispensedByPharmacy(null);

        SerializedUnit saved = serializedUnitRepository.save(unit);

        statusTransitionService.recordTransition(
                StatusTransition.EntityType.SERIALIZED_UNIT,
                unitId,
                oldStatus.name(),
                SerializedUnit.UnitStatus.ACTIVE.name(),
                reason,
                userId);

        auditService.log("UNIT_REVERT_DECOMMISSION", "SerializedUnit", unitId, userId,
                Map.of("serialNumber", unit.getSerialNumber(), "reason", reason));

        return SerializedUnitResponse.from(saved);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "serializedUnits", key = "#id")
    public SerializedUnitResponse getUnitById(Long id) {
        SerializedUnit unit = serializedUnitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Serialized unit not found"));
        return SerializedUnitResponse.from(unit);
    }

    @Transactional(readOnly = true)
    public SerializedUnitResponse getUnitBySerialNumber(String serialNumber) {
        SerializedUnit unit = serializedUnitRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new RuntimeException("Serialized unit not found"));
        return SerializedUnitResponse.from(unit);
    }

    @Transactional(readOnly = true)
    public Page<SerializedUnitResponse> getUnitsByBatch(Long batchId, Pageable pageable) {
        return serializedUnitRepository.findByBatchId(batchId)
                .stream()
                .map(SerializedUnitResponse::from)
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toList(),
                        list -> new org.springframework.data.domain.PageImpl<>(
                                list.subList(
                                        (int) pageable.getOffset(),
                                        Math.min((int) (pageable.getOffset() + pageable.getPageSize()), list.size())),
                                pageable,
                                list.size())));
    }

    @Transactional(readOnly = true)
    public Page<SerializedUnitResponse> getUnitsByStatus(SerializedUnit.UnitStatus status, Pageable pageable) {
        return serializedUnitRepository.findByStatus(status)
                .stream()
                .map(SerializedUnitResponse::from)
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toList(),
                        list -> new org.springframework.data.domain.PageImpl<>(
                                list.subList(
                                        (int) pageable.getOffset(),
                                        Math.min((int) (pageable.getOffset() + pageable.getPageSize()), list.size())),
                                pageable,
                                list.size())));
    }
}
