package com.pharma.drugverification.controller;

import com.pharma.drugverification.domain.Drug;
import com.pharma.drugverification.dto.DrugApprovalRequest;
import com.pharma.drugverification.dto.DrugRegistrationRequest;
import com.pharma.drugverification.dto.DrugResponse;
import com.pharma.drugverification.service.DrugService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/drugs")
@RequiredArgsConstructor
public class DrugController {

    private final DrugService drugService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MANUFACTURER', 'ADMIN')")
    public ResponseEntity<DrugResponse> registerDrug(
            @Valid @RequestBody DrugRegistrationRequest request,
            @RequestAttribute("userId") Long userId) {
        DrugResponse response = drugService.registerDrug(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('REGULATOR', 'ADMIN')")
    public ResponseEntity<DrugResponse> approveDrug(
            @PathVariable Long id,
            @RequestAttribute("userId") Long regulatorId) {
        DrugResponse response = drugService.approveDrug(id, regulatorId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('REGULATOR', 'ADMIN')")
    public ResponseEntity<DrugResponse> rejectDrug(
            @PathVariable Long id,
            @Valid @RequestBody DrugApprovalRequest request,
            @RequestAttribute("userId") Long regulatorId) {
        DrugResponse response = drugService.rejectDrug(id, request.getReason(), regulatorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DrugResponse> getDrugById(@PathVariable Long id) {
        DrugResponse response = drugService.getDrugById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ndc/{ndc}")
    public ResponseEntity<DrugResponse> getDrugByNdc(@PathVariable String ndc) {
        DrugResponse response = drugService.getDrugByNdc(ndc);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<DrugResponse>> getAllDrugs(Pageable pageable) {
        Page<DrugResponse> response = drugService.getAllDrugs(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<DrugResponse>> getDrugsByStatus(
            @PathVariable Drug.DrugStatus status,
            Pageable pageable) {
        Page<DrugResponse> response = drugService.getDrugsByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/manufacturer/{manufacturerId}")
    public ResponseEntity<Page<DrugResponse>> getDrugsByManufacturer(
            @PathVariable Long manufacturerId,
            Pageable pageable) {
        Page<DrugResponse> response = drugService.getDrugsByManufacturer(manufacturerId, pageable);
        return ResponseEntity.ok(response);
    }
}
