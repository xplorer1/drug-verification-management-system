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
@Table(name = "alerts", indexes = {
        @Index(name = "idx_alert_type", columnList = "alertType"),
        @Index(name = "idx_severity", columnList = "severity"),
        @Index(name = "idx_created_at", columnList = "createdAt"),
        @Index(name = "idx_acknowledged", columnList = "acknowledged")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AlertType alertType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AlertSeverity severity;

    @NotBlank
    @Column(nullable = false, length = 500)
    private String message;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column
    private Long relatedEntityId;

    @Column(length = 50)
    private String relatedEntityType;

    @Column(nullable = false)
    private Boolean acknowledged = false;

    @Column
    private Long acknowledgedByUserId;

    @Column
    private LocalDateTime acknowledgedAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum AlertType {
        COUNTERFEIT_DETECTED,
        DISTANCE_TIME_COLLISION,
        TEMPERATURE_EXCURSION,
        RECALL_INITIATED,
        DUPLICATE_SCAN,
        ANOMALOUS_FAILURE_RATE,
        GEOGRAPHIC_CLUSTERING
    }

    public enum AlertSeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}
