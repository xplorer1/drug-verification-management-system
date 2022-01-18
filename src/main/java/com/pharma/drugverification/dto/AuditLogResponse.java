package com.pharma.drugverification.dto;

import com.pharma.drugverification.domain.AuditLog;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class AuditLogResponse {
    private Long id;
    private String action;
    private String entityType;
    private Long entityId;
    private Long userId;
    private String username;
    private String ipAddress;
    private String userAgent;
    private Map<String, Object> changes;
    private String currentHash;
    private String previousHash;
    private Boolean blockchainAnchored;
    private String blockchainTransactionId;
    private LocalDateTime createdAt;

    public static AuditLogResponse from(AuditLog log) {
        AuditLogResponse response = new AuditLogResponse();
        response.setId(log.getId());
        response.setAction(log.getAction());
        response.setEntityType(log.getEntityType());
        response.setEntityId(log.getEntityId());
        response.setUserId(log.getUserId());
        response.setUsername(log.getUsername());
        response.setIpAddress(log.getIpAddress());
        response.setUserAgent(log.getUserAgent());
        response.setChanges(log.getChanges());
        response.setCurrentHash(log.getCurrentHash());
        response.setPreviousHash(log.getPreviousHash());
        response.setBlockchainTransactionId(log.getBlockchainTransactionId());
        response.setBlockchainAnchored(log.getBlockchainTransactionId() != null);
        response.setCreatedAt(log.getCreatedAt());
        return response;
    }
}
