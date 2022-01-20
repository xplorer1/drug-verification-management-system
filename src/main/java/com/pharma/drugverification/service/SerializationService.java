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
import com.pharma.drugverification.exception.BadRequestException;
import com.pharma.drugverification.exception.ResourceNotFoundException;

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
                        throw new BadRequestException(
                                        "Unit with serial number " + request.getSerialNumber() + " already exists");
                }

                Batch batch = batchRepository.findById(request.getBatchId())
                                .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));

                if (batch.getStatus() != Batch.BatchStatus.ACTIVE) {
                        throw new BadRequestException("Cannot serialize units for inactive batch");
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
                                .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));

                if (batch.getStatus() != Batch.BatchStatus.ACTIVE) {
                        throw new BadRequestException("Cannot serialize units for inactive batch");
                }

                String expirationDate = batch.getExpirationDate()
                                .format(DateTimeFormatter.ofPattern("yyMMdd"));
                int currentKeyVersion = hsmService.getCurrentKeyVersion();

                List<SerializedUnit> units = new ArrayList<>();

                for (int i = 0; i < quantity; i++) {
                        String serialNumber = hsmService.generateSerialNumber();

                        String cryptoTail = hsmService.generateCryptoTail(
                                        serialNumber,
                                        gtin,
                                        batch.getBatchNumber());

                        String dataMatrix = hsmService.generateDataMatrix(
                                        gtin,
                                        serialNumber,
                                        batch.getBatchNumber(),
                                        expirationDate);

                        SerializedUnit unit = new SerializedUnit();
                        unit.setSerialNumber(serialNumber);
                        unit.setBatchId(batchId);
                        unit.setGtin(gtin);
                        unit.setCryptoTail(cryptoTail);
                        unit.setDataMatrix(dataMatrix);
                        unit.setKeyVersion(currentKeyVersion);
                        unit.setStatus(SerializedUnit.UnitStatus.ACTIVE);

                        units.add(unit);
                }

                List<SerializedUnit> savedUnits = serializedUnitRepository.saveAll(units);

                auditService.log("UNIT_BULK_SERIALIZED", "Batch", batchId, userId,
                                Map.of("gtin", gtin, "quantity", quantity));

                return savedUnits.stream()
                                .map(SerializedUnitResponse::from)
                                .toList();
        }

        @Transactional
        @CacheEvict(value = "serializedUnits", allEntries = true)
        public SerializedUnitResponse decommissionUnit(Long unitId, Long userId, String pharmacy) {
                SerializedUnit unit = serializedUnitRepository.findById(unitId)
                                .orElseThrow(() -> new ResourceNotFoundException("Serialized unit not found"));

                if (unit.getStatus() == SerializedUnit.UnitStatus.DISPENSED) {
                        throw new BadRequestException("Unit is already dispensed");
                }

                if (unit.getStatus() == SerializedUnit.UnitStatus.DESTROYED) {
                        throw new BadRequestException("Unit is already destroyed");
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
                                .orElseThrow(() -> new ResourceNotFoundException("Serialized unit not found"));

                if (unit.getStatus() != SerializedUnit.UnitStatus.DISPENSED) {
                        throw new BadRequestException("Unit is not dispensed");
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
                                .orElseThrow(() -> new ResourceNotFoundException("Serialized unit not found"));
                return SerializedUnitResponse.from(unit);
        }

        @Transactional(readOnly = true)
        public SerializedUnitResponse getUnitBySerialNumber(String serialNumber) {
                SerializedUnit unit = serializedUnitRepository.findBySerialNumber(serialNumber)
                                .orElseThrow(() -> new ResourceNotFoundException("Serialized unit not found"));
                return SerializedUnitResponse.from(unit);
        }

        @Transactional(readOnly = true)
        public Page<SerializedUnitResponse> getUnitsByBatch(Long batchId, Pageable pageable) {
                return serializedUnitRepository.findByBatchId(batchId, pageable)
                                .map(SerializedUnitResponse::from);
        }

        @Transactional(readOnly = true)
        public Page<SerializedUnitResponse> getUnitsByStatus(SerializedUnit.UnitStatus status, Pageable pageable) {
                return serializedUnitRepository.findByStatus(status, pageable)
                                .map(SerializedUnitResponse::from);
        }
}
