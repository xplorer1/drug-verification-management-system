package com.pharma.drugverification.controller;

import com.pharma.drugverification.domain.Aggregation;
import com.pharma.drugverification.dto.AggregationRequest;
import com.pharma.drugverification.dto.AggregationResponse;
import com.pharma.drugverification.service.AggregationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/aggregations")
@RequiredArgsConstructor
public class AggregationController {

    private final AggregationService aggregationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MANUFACTURER', 'DISTRIBUTOR', 'ADMIN')")
    public ResponseEntity<AggregationResponse> createAggregation(
            @Valid @RequestBody AggregationRequest request,
            @RequestAttribute("userId") Long userId) {
        AggregationResponse response = aggregationService.createAggregation(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/disaggregate")
    @PreAuthorize("hasAnyRole('MANUFACTURER', 'DISTRIBUTOR', 'ADMIN')")
    public ResponseEntity<AggregationResponse> disaggregate(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        AggregationResponse response = aggregationService.disaggregate(id, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AggregationResponse> getAggregation(@PathVariable Long id) {
        AggregationResponse response = aggregationService.getAggregation(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<AggregationResponse>> getAggregationsByType(
            @PathVariable Aggregation.AggregationType type) {
        List<AggregationResponse> response = aggregationService.getAggregationsByType(type);
        return ResponseEntity.ok(response);
    }
}
