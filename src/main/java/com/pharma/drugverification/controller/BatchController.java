package com.pharma.drugverification.controller;

import com.pharma.drugverification.domain.Batch;
import com.pharma.drugverification.dto.BatchCreationRequest;
import com.pharma.drugverification.dto.BatchResponse;
import com.pharma.drugverification.dto.BatchUpdateRequest;
import com.pharma.drugverification.service.BatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/batches")
@RequiredArgsConstructor
@Tag(name = "Batches", description = "Endpoints for batch creation, status management, and tracking")
public class BatchController {

    private final BatchService batchService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MANUFACTURER', 'ADMIN')")
    @Operation(summary = "Create a new batch", description = "Allows manufacturers to initialize a new manufacturing batch")
    public ResponseEntity<BatchResponse> createBatch(
            @Valid @RequestBody BatchCreationRequest request,
            @RequestAttribute("userId") Long userId) {
        BatchResponse response = batchService.createBatch(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANUFACTURER', 'DISTRIBUTOR', 'ADMIN')")
    public ResponseEntity<BatchResponse> updateBatch(
            @PathVariable Long id,
            @Valid @RequestBody BatchUpdateRequest request,
            @RequestAttribute("userId") Long userId) {
        BatchResponse response = batchService.updateBatch(id, request, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status/{status}")
    @PreAuthorize("hasAnyRole('MANUFACTURER', 'DISTRIBUTOR', 'REGULATOR', 'ADMIN')")
    @Operation(summary = "Update batch status", description = "Promotes batch through lifecycle (MANUFACTURED -> RELEASED -> etc.)")
    public ResponseEntity<BatchResponse> updateBatchStatus(
            @PathVariable Long id,
            @PathVariable Batch.BatchStatus status,
            @RequestParam(required = false) String reason,
            @RequestAttribute("userId") Long userId) {
        BatchResponse response = batchService.updateBatchStatus(id, status, reason, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get batch by ID", description = "Retrieves batch details and status by its internal ID")
    public ResponseEntity<BatchResponse> getBatchById(@PathVariable Long id) {
        BatchResponse response = batchService.getBatchById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/number/{batchNumber}")
    public ResponseEntity<BatchResponse> getBatchByNumber(@PathVariable String batchNumber) {
        BatchResponse response = batchService.getBatchByNumber(batchNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<BatchResponse>> getAllBatches(Pageable pageable) {
        Page<BatchResponse> response = batchService.getAllBatches(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/drug/{drugId}")
    public ResponseEntity<Page<BatchResponse>> getBatchesByDrug(
            @PathVariable Long drugId,
            Pageable pageable) {
        Page<BatchResponse> response = batchService.getBatchesByDrug(drugId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<BatchResponse>> getBatchesByStatus(
            @PathVariable Batch.BatchStatus status,
            Pageable pageable) {
        Page<BatchResponse> response = batchService.getBatchesByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }
}
