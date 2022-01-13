package com.pharma.drugverification.controller;

import com.pharma.drugverification.dto.VerificationRequest;
import com.pharma.drugverification.dto.VerificationResponse;
import com.pharma.drugverification.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/verify")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    @PostMapping
    public ResponseEntity<VerificationResponse> verifyUnit(
            @Valid @RequestBody VerificationRequest request,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        VerificationResponse response = verificationService.verifyUnit(
                request.getSerialNumber(),
                request.getLatitude(),
                request.getLongitude(),
                request.getLocation(),
                request.getDeviceId(),
                userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getVerificationStats(
            @RequestParam(required = false) Integer daysAgo) {
        LocalDateTime since = LocalDateTime.now().minusDays(daysAgo != null ? daysAgo : 30);
        Map<String, Object> stats = verificationService.getVerificationStats(since);
        return ResponseEntity.ok(stats);
    }
}
