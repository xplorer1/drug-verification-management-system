package com.pharma.drugverification.service;

import com.pharma.drugverification.domain.AuditLog;
import com.pharma.drugverification.dto.AuditLogResponse;
import com.pharma.drugverification.repository.AuditLogRepository;
import com.pharma.drugverification.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
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
    private final UserRepository userRepository;

    @Async
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

            // Enrich with request details if available
            enrichWithRequestDetails(auditLog);
            // Enrich with username
            enrichWithUsername(auditLog, userId);

            String currentHash = calculateHash(auditLog, previousHash);
            auditLog.setCurrentHash(currentHash);

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }

    private void enrichWithRequestDetails(AuditLog log) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                log.setIpAddress(request.getRemoteAddr());
                log.setUserAgent(request.getHeader("User-Agent"));
            }
        } catch (Exception e) {
            // Ignore if not in web context
        }
    }

    private void enrichWithUsername(AuditLog log, Long userId) {
        userRepository.findById(userId).ifPresent(user -> log.setUsername(user.getUsername()));
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

    public Page<AuditLogResponse> getLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable).map(AuditLogResponse::from);
    }

    public Page<AuditLogResponse> getLogsByEntity(String entityType, Long entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable)
                .map(AuditLogResponse::from);
    }

    public Page<AuditLogResponse> getLogsByUser(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable).map(AuditLogResponse::from);
    }
}
