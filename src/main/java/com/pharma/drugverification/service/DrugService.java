package com.pharma.drugverification.service;

import com.pharma.drugverification.domain.Drug;
import com.pharma.drugverification.domain.StatusTransition;
import com.pharma.drugverification.dto.DrugRegistrationRequest;
import com.pharma.drugverification.dto.DrugResponse;
import com.pharma.drugverification.repository.DrugRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DrugService {

    private final DrugRepository drugRepository;
    private final StatusTransitionService statusTransitionService;
    private final AuditService auditService;

    @Transactional
    @CacheEvict(value = "drugs", allEntries = true)
    public DrugResponse registerDrug(DrugRegistrationRequest request, Long userId) {
        if (drugRepository.existsByNdc(request.getNdc())) {
            throw new RuntimeException("Drug with NDC " + request.getNdc() + " already exists");
        }

        String cryptoIdentifier = generateCryptoIdentifier(request.getNdc(), request.getName());

        Drug drug = new Drug();
        drug.setName(request.getName());
        drug.setCryptoIdentifier(cryptoIdentifier);
        drug.setNdc(request.getNdc());
        drug.setManufacturer(request.getManufacturer());
        drug.setManufacturerId(request.getManufacturerId());
        drug.setDescription(request.getDescription());
        drug.setDosageForm(request.getDosageForm());
        drug.setStrength(request.getStrength());
        drug.setStatus(Drug.DrugStatus.PENDING);
        drug.setMinTemperature(request.getMinTemperature());
        drug.setMaxTemperature(request.getMaxTemperature());
        drug.setRegulatorId(1L); // Default regulator, should be configurable

        Drug saved = drugRepository.save(drug);

        auditService.log("DRUG_REGISTERED", "Drug", saved.getId(), userId,
                Map.of("ndc", request.getNdc(), "name", request.getName()));

        return DrugResponse.from(saved);
    }

    @Transactional
    @CacheEvict(value = "drugs", allEntries = true)
    public DrugResponse approveDrug(Long drugId, Long regulatorId) {
        Drug drug = drugRepository.findById(drugId)
                .orElseThrow(() -> new RuntimeException("Drug not found"));

        if (drug.getStatus() != Drug.DrugStatus.PENDING) {
            throw new RuntimeException("Drug is not in PENDING status");
        }

        Drug.DrugStatus oldStatus = drug.getStatus();
        drug.setStatus(Drug.DrugStatus.APPROVED);
        drug.setApprovedAt(LocalDateTime.now());
        drug.setRejectionReason(null);

        Drug saved = drugRepository.save(drug);

        statusTransitionService.recordTransition(
                StatusTransition.EntityType.DRUG,
                drugId,
                oldStatus.name(),
                Drug.DrugStatus.APPROVED.name(),
                "Drug approved",
                regulatorId);

        auditService.log("DRUG_APPROVED", "Drug", drugId, regulatorId,
                Map.of("drugName", drug.getName(), "ndc", drug.getNdc()));

        return DrugResponse.from(saved);
    }

    @Transactional
    @CacheEvict(value = "drugs", allEntries = true)
    public DrugResponse rejectDrug(Long drugId, String reason, Long regulatorId) {
        Drug drug = drugRepository.findById(drugId)
                .orElseThrow(() -> new RuntimeException("Drug not found"));

        if (drug.getStatus() != Drug.DrugStatus.PENDING) {
            throw new RuntimeException("Drug is not in PENDING status");
        }

        Drug.DrugStatus oldStatus = drug.getStatus();
        drug.setStatus(Drug.DrugStatus.REJECTED);
        drug.setRejectionReason(reason);

        Drug saved = drugRepository.save(drug);

        statusTransitionService.recordTransition(
                StatusTransition.EntityType.DRUG,
                drugId,
                oldStatus.name(),
                Drug.DrugStatus.REJECTED.name(),
                reason,
                regulatorId);

        auditService.log("DRUG_REJECTED", "Drug", drugId, regulatorId,
                Map.of("drugName", drug.getName(), "ndc", drug.getNdc(), "reason", reason));

        return DrugResponse.from(saved);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "drugs", key = "#id")
    public DrugResponse getDrugById(Long id) {
        Drug drug = drugRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Drug not found"));
        return DrugResponse.from(drug);
    }

    @Transactional(readOnly = true)
    public DrugResponse getDrugByNdc(String ndc) {
        Drug drug = drugRepository.findByNdc(ndc)
                .orElseThrow(() -> new RuntimeException("Drug not found"));
        return DrugResponse.from(drug);
    }

    @Transactional(readOnly = true)
    public Page<DrugResponse> getDrugsByStatus(Drug.DrugStatus status, Pageable pageable) {
        return drugRepository.findByStatus(status, pageable)
                .map(DrugResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<DrugResponse> getDrugsByManufacturer(Long manufacturerId, Pageable pageable) {
        return drugRepository.findByManufacturerId(manufacturerId, pageable)
                .map(DrugResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<DrugResponse> getAllDrugs(Pageable pageable) {
        return drugRepository.findAll(pageable)
                .map(DrugResponse::from);
    }

    private String generateCryptoIdentifier(String ndc, String name) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String data = ndc + ":" + name + ":" + System.currentTimeMillis();
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
