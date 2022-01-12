package com.pharma.drugverification.controller;

import com.pharma.drugverification.domain.SerializedUnit;
import com.pharma.drugverification.dto.SerializedUnitCreationRequest;
import com.pharma.drugverification.dto.SerializedUnitResponse;
import com.pharma.drugverification.service.SerializationService;
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
@RequestMapping("/api/v1/units")
@RequiredArgsConstructor
public class SerializationController {

    private final SerializationService serializationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MANUFACTURER', 'ADMIN')")
    public ResponseEntity<SerializedUnitResponse> createUnit(
            @Valid @RequestBody SerializedUnitCreationRequest request,
            @RequestAttribute("userId") Long userId) {
        SerializedUnitResponse response = serializationService.createSerializedUnit(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('MANUFACTURER', 'ADMIN')")
    public ResponseEntity<List<SerializedUnitResponse>> bulkCreateUnits(
            @RequestParam Long batchId,
            @RequestParam String gtin,
            @RequestParam int quantity,
            @RequestAttribute("userId") Long userId) {
        List<SerializedUnitResponse> responses = serializationService.bulkCreateSerializedUnits(
                batchId, gtin, quantity, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @PostMapping("/{id}/decommission")
    @PreAuthorize("hasAnyRole('PHARMACIST', 'ADMIN')")
    public ResponseEntity<SerializedUnitResponse> decommissionUnit(
            @PathVariable Long id,
            @RequestParam String pharmacy,
            @RequestAttribute("userId") Long userId) {
        SerializedUnitResponse response = serializationService.decommissionUnit(id, userId, pharmacy);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/revert")
    @PreAuthorize("hasAnyRole('PHARMACIST', 'ADMIN')")
    public ResponseEntity<SerializedUnitResponse> revertDecommission(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestAttribute("userId") Long userId) {
        SerializedUnitResponse response = serializationService.revertDecommission(id, reason, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SerializedUnitResponse> getUnitById(@PathVariable Long id) {
        SerializedUnitResponse response = serializationService.getUnitById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/serial/{serialNumber}")
    public ResponseEntity<SerializedUnitResponse> getUnitBySerialNumber(@PathVariable String serialNumber) {
        SerializedUnitResponse response = serializationService.getUnitBySerialNumber(serialNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/batch/{batchId}")
    public ResponseEntity<Page<SerializedUnitResponse>> getUnitsByBatch(
            @PathVariable Long batchId,
            Pageable pageable) {
        Page<SerializedUnitResponse> response = serializationService.getUnitsByBatch(batchId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<SerializedUnitResponse>> getUnitsByStatus(
            @PathVariable SerializedUnit.UnitStatus status,
            Pageable pageable) {
        Page<SerializedUnitResponse> response = serializationService.getUnitsByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }
}
