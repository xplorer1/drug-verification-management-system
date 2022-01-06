package com.pharma.drugverification.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_action", columnList = "action"),
        @Index(name = "idx_entity_type_id", columnList = "entityType,entityId"),
        @Index(name = "idx_user_id", columnList = "userId"),
        @Index(name = "idx_created_at", columnList = "createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String action;

    @Column(length = 50)
    private String entityType;

    @Column
    private Long entityId;

    @Column(nullable = false)
    private Long userId;

    @Column(length = 100)
    private String username;

    @Column(length = 100)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> changes;

    @Column(nullable = false, length = 64)
    private String currentHash;

    @Column(length = 64)
    private String previousHash;

    @Column
    private String blockchainTransactionId;

    @Column
    private LocalDateTime blockchainAnchoredAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
