package com.pharma.drugverification.service;

import com.pharma.drugverification.domain.AuditLog;
import com.pharma.drugverification.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(String action, String entityType, Long entityId, Long userId, Map<String, Object> changes) {
        try {
            AuditLog previousLog = auditLogRepository.findTopByOrderByIdDesc().orElse(null);
            String previousHash = previousLog != null ? previousLog.getCurrentHash() : "0";

            AuditLog auditLog = new AuditLog();
            auditLog.setAction(action);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setUserId(userId);
            auditLog.setChanges(changes);
            auditLog.setPreviousHash(previousHash);

            String currentHash = calculateHash(auditLog, previousHash);
            auditLog.setCurrentHash(currentHash);

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }

    private String calculateHash(AuditLog log, String previousHash) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String data = log.getAction() +
                    log.getEntityType() +
                    log.getEntityId() +
                    log.getUserId() +
                    previousHash +
                    LocalDateTime.now().toString();
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
