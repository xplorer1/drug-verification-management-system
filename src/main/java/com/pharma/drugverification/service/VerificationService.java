package com.pharma.drugverification.service;

import com.pharma.drugverification.config.ApplicationProperties;
import com.pharma.drugverification.domain.Batch;
import com.pharma.drugverification.domain.Recall;
import com.pharma.drugverification.domain.SerializedUnit;
import com.pharma.drugverification.dto.VerificationResponse;
import com.pharma.drugverification.repository.BatchRepository;
import com.pharma.drugverification.repository.RecallRepository;
import com.pharma.drugverification.repository.SerializedUnitRepository;
import com.pharma.drugverification.repository.VerificationRequestRepository;
import com.pharma.drugverification.security.HsmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationService {

    private final SerializedUnitRepository serializedUnitRepository;
    private final BatchRepository batchRepository;
    private final RecallRepository recallRepository;
    private final VerificationRequestRepository verificationRequestRepository;
    private final HsmService hsmService;
    private final AuditService auditService;
    private final AlertService alertService;
    private final ApplicationProperties applicationProperties;

    @Transactional
    public VerificationResponse verifyUnit(
            String serialNumber,
            Double latitude,
            Double longitude,
            String location,
            String deviceId,
            Long userId) {
        long startTime = System.currentTimeMillis();
        VerificationResponse response = new VerificationResponse();
        response.setSerialNumber(serialNumber);
        response.setVerifiedAt(LocalDateTime.now());

        // Find the serialized unit
        SerializedUnit unit = serializedUnitRepository.findBySerialNumber(serialNumber).orElse(null);

        if (unit == null) {
            response.setResult(com.pharma.drugverification.domain.VerificationRequest.VerificationResult.NOT_FOUND);
            response.setIsValid(false);
            response.setMessage("Serial number not found in system");
            recordVerificationRequest(serialNumber, null, response.getResult(), latitude, longitude, location, deviceId,
                    userId, startTime);
            return response;
        }

        // Get batch information
        Batch batch = batchRepository.findById(unit.getBatchId()).orElse(null);
        if (batch == null) {
            response.setResult(com.pharma.drugverification.domain.VerificationRequest.VerificationResult.INVALID);
            response.setIsValid(false);
            response.setMessage("Batch information not found");
            recordVerificationRequest(serialNumber, unit.getId(), response.getResult(), latitude, longitude, location,
                    deviceId, userId, startTime);
            return response;
        }

        response.setBatchNumber(batch.getBatchNumber());
        response.setExpirationDate(batch.getExpirationDate().atStartOfDay());

        // Verify crypto-tail
        boolean cryptoValid = hsmService.verifyCryptoTail(
                unit.getSerialNumber(),
                unit.getGtin(),
                batch.getBatchNumber(),
                unit.getCryptoTail());

        if (!cryptoValid) {
            response.setResult(com.pharma.drugverification.domain.VerificationRequest.VerificationResult.INVALID);
            response.setIsValid(false);
            response.setMessage("Crypto-tail verification failed - possible counterfeit");

            alertService.createAlert(
                    "COUNTERFEIT_DETECTED",
                    "High",
                    "Crypto-tail verification failed for serial: " + serialNumber,
                    "SerializedUnit",
                    unit.getId());

            recordVerificationRequest(serialNumber, unit.getId(), response.getResult(), latitude, longitude, location,
                    deviceId, userId, startTime);
            return response;
        }

        // Check for recalls
        boolean isRecalled = recallRepository.existsByBatchIdAndStatus(
                batch.getId(),
                Recall.RecallStatus.ACTIVE);

        if (isRecalled) {
            response.setResult(com.pharma.drugverification.domain.VerificationRequest.VerificationResult.RECALLED);
            response.setIsValid(false);
            response.setMessage("This product has been recalled");
            recordVerificationRequest(serialNumber, unit.getId(), response.getResult(), latitude, longitude, location,
                    deviceId, userId, startTime);
            return response;
        }

        // Check expiration
        if (batch.getExpirationDate().isBefore(LocalDate.now())) {
            response.setResult(com.pharma.drugverification.domain.VerificationRequest.VerificationResult.EXPIRED);
            response.setIsValid(false);
            response.setMessage("Product has expired");
            response.addWarning("Expired on " + batch.getExpirationDate());
            recordVerificationRequest(serialNumber, unit.getId(), response.getResult(), latitude, longitude, location,
                    deviceId, userId, startTime);
            return response;
        }

        // Check unit status
        if (unit.getStatus() == SerializedUnit.UnitStatus.QUARANTINED) {
            response.setResult(com.pharma.drugverification.domain.VerificationRequest.VerificationResult.QUARANTINED);
            response.setIsValid(false);
            response.setMessage("Product is quarantined");
            recordVerificationRequest(serialNumber, unit.getId(), response.getResult(), latitude, longitude, location,
                    deviceId, userId, startTime);
            return response;
        }

        if (unit.getStatus() == SerializedUnit.UnitStatus.DISPENSED) {
            response.setResult(
                    com.pharma.drugverification.domain.VerificationRequest.VerificationResult.ALREADY_DISPENSED);
            response.setIsValid(false);
            response.setMessage("Product has already been dispensed");
            response.addWarning("Dispensed at: " + unit.getDispensedByPharmacy());
            recordVerificationRequest(serialNumber, unit.getId(), response.getResult(), latitude, longitude, location,
                    deviceId, userId, startTime);
            return response;
        }

        // Check for duplicate scans (within last hour)
        checkForDuplicateScans(unit.getId(), response);

        // Check for distance-time collision
        if (latitude != null && longitude != null) {
            checkDistanceTimeCollision(serialNumber, latitude, longitude, response);
        }

        // All checks passed
        response.setResult(com.pharma.drugverification.domain.VerificationRequest.VerificationResult.VALID);
        response.setIsValid(true);
        response.setMessage("Product is authentic and valid");
        response.setDrugName(batch.getDrug() != null ? batch.getDrug().getName() : "Unknown");
        response.setManufacturer(batch.getDrug() != null ? batch.getDrug().getManufacturer() : "Unknown");

        recordVerificationRequest(serialNumber, unit.getId(), response.getResult(), latitude, longitude, location,
                deviceId, userId, startTime);

        auditService.log("UNIT_VERIFIED", "SerializedUnit", unit.getId(), userId,
                Map.of("serialNumber", serialNumber, "result", response.getResult().name()));

        return response;
    }

    private void checkForDuplicateScans(Long unitId, VerificationResponse response) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minus(1, ChronoUnit.HOURS);
        long recentScans = verificationRequestRepository.countByResultAndCreatedAtAfter(
                com.pharma.drugverification.domain.VerificationRequest.VerificationResult.VALID,
                oneHourAgo);

        if (recentScans > 3) {
            response.addWarning("Multiple verification attempts detected in the last hour");

            alertService.createAlert(
                    "DUPLICATE_SCAN",
                    "Medium",
                    "Multiple scans detected for unit ID: " + unitId,
                    "SerializedUnit",
                    unitId);
        }
    }

    private void checkDistanceTimeCollision(String serialNumber, Double latitude, Double longitude,
            VerificationResponse response) {
        LocalDateTime recentTime = LocalDateTime.now().minus(
                applicationProperties.getVerification().getMinTimeBetweenScansMinutes(),
                ChronoUnit.MINUTES);

        List<com.pharma.drugverification.domain.VerificationRequest> recentRequests = verificationRequestRepository
                .findRecentVerificationsBySerial(
                        serialNumber,
                        recentTime,
                        LocalDateTime.now());

        for (com.pharma.drugverification.domain.VerificationRequest req : recentRequests) {
            if (req.getLatitude() != null && req.getLongitude() != null) {
                double distance = calculateDistance(
                        latitude, longitude,
                        req.getLatitude(), req.getLongitude());

                double maxDistance = applicationProperties.getVerification().getMaxDistanceKm();

                if (distance > maxDistance) {
                    response.addWarning(
                            String.format("Suspicious scanning pattern: %.2f km away from previous scan", distance));

                    alertService.createAlert(
                            "DISTANCE_TIME_COLLISION",
                            "High",
                            "Unit scanned " + distance + " km apart in short time for serial: " + serialNumber,
                            "SerializedUnit",
                            null);
                }
            }
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula for calculating distance between two coordinates
        final int EARTH_RADIUS_KM = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    private void recordVerificationRequest(
            String serialNumber,
            Long unitId,
            com.pharma.drugverification.domain.VerificationRequest.VerificationResult result,
            Double latitude,
            Double longitude,
            String location,
            String deviceId,
            Long userId,
            long startTime) {
        com.pharma.drugverification.domain.VerificationRequest request = new com.pharma.drugverification.domain.VerificationRequest();

        request.setSerialNumber(serialNumber);
        request.setSerializedUnitId(unitId);
        request.setResult(result);
        request.setLatitude(latitude);
        request.setLongitude(longitude);
        request.setLocation(location);
        request.setDeviceId(deviceId);
        request.setRequestedByUserId(userId);
        request.setResponseTimeMs(System.currentTimeMillis() - startTime);
        request.setPossibleCounterfeit(
                result == com.pharma.drugverification.domain.VerificationRequest.VerificationResult.INVALID);

        verificationRequestRepository.save(request);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "verificationStats")
    public Map<String, Object> getVerificationStats(LocalDateTime since) {
        long totalVerifications = verificationRequestRepository.count();
        long validVerifications = verificationRequestRepository.countByResultAndCreatedAtAfter(
                com.pharma.drugverification.domain.VerificationRequest.VerificationResult.VALID,
                since);
        long invalidVerifications = verificationRequestRepository.countByResultAndCreatedAtAfter(
                com.pharma.drugverification.domain.VerificationRequest.VerificationResult.INVALID,
                since);
        Double avgResponseTime = verificationRequestRepository.getAverageResponseTime(since);

        return Map.of(
                "totalVerifications", totalVerifications,
                "validVerifications", validVerifications,
                "invalidVerifications", invalidVerifications,
                "averageResponseTimeMs", avgResponseTime != null ? avgResponseTime : 0.0);
    }
}
