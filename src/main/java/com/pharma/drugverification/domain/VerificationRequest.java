package com.pharma.drugverification.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_requests", indexes = {
        @Index(name = "idx_serial_number", columnList = "serialNumber"),
        @Index(name = "idx_created_at", columnList = "createdAt"),
        @Index(name = "idx_result", columnList = "result")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class VerificationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String serialNumber;

    @Column
    private Long serializedUnitId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private VerificationResult result;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(length = 100)
    private String location;

    @Column(length = 200)
    private String deviceId;

    @Column
    private Long requestedByUserId;

    @Column
    private Long responseTimeMs;

    @Column(columnDefinition = "TEXT")
    private String warnings;

    @Column
    private Boolean possibleCounterfeit;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum VerificationResult {
        VALID,
        INVALID,
        RECALLED,
        EXPIRED,
        QUARANTINED,
        ALREADY_DISPENSED,
        NOT_FOUND
    }
}
