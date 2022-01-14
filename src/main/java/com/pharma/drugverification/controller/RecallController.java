package com.pharma.drugverification.controller;

import com.pharma.drugverification.domain.Recall;
import com.pharma.drugverification.dto.RecallRequest;
import com.pharma.drugverification.dto.RecallResponse;
import com.pharma.drugverification.service.RecallService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recalls")
@RequiredArgsConstructor
public class RecallController {

    private final RecallService recallService;

    @PostMapping
    @PreAuthorize("hasAnyRole('REGULATOR', 'ADMIN')")
    public ResponseEntity<RecallResponse> initiateRecall(
            @Valid @RequestBody RecallRequest request,
            @RequestAttribute("userId") Long regulatorId) {
        RecallResponse response = recallService.initiateRecall(request, regulatorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{recallId}/recover/{unitId}")
    @PreAuthorize("hasAnyRole('REGULATOR', 'PHARMACIST', 'ADMIN')")
    public ResponseEntity<RecallResponse> recordRecovery(
            @PathVariable Long recallId,
            @PathVariable Long unitId,
            @RequestAttribute("userId") Long userId) {
        RecallResponse response = recallService.recordRecovery(recallId, unitId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('REGULATOR', 'ADMIN')")
    public ResponseEntity<RecallResponse> completeRecall(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        RecallResponse response = recallService.completeRecall(id, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecallResponse> getRecall(@PathVariable Long id) {
        RecallResponse response = recallService.getRecall(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<List<RecallResponse>> getActiveRecalls() {
        List<RecallResponse> response = recallService.getActiveRecalls();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<RecallResponse>> getRecallsByStatus(
            @PathVariable Recall.RecallStatus status,
            Pageable pageable) {
        Page<RecallResponse> response = recallService.getRecallsByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }
}
