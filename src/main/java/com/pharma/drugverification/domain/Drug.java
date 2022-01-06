package com.pharma.drugverification.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "drugs", indexes = {
        @Index(name = "idx_crypto_identifier", columnList = "cryptoIdentifier", unique = true),
        @Index(name = "idx_ndc", columnList = "ndc"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_manufacturer_id", columnList = "manufacturerId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Drug {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String name;

    @NotBlank
    @Column(nullable = false, unique = true, length = 64)
    private String cryptoIdentifier;

    @NotBlank
    @Column(nullable = false, unique = true, length = 20)
    private String ndc;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String manufacturer;

    @NotNull
    @Column(nullable = false)
    private Long manufacturerId;

    @Column(length = 2000)
    private String description;

    @Column(length = 100)
    private String dosageForm;

    @Column(length = 100)
    private String strength;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DrugStatus status = DrugStatus.PENDING;

    @Column
    private BigDecimal minTemperature;

    @Column
    private BigDecimal maxTemperature;

    @Column(length = 1000)
    private String rejectionReason;

    @NotNull
    @Column(nullable = false)
    private Long regulatorId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime approvedAt;

    public enum DrugStatus {
        PENDING,
        APPROVED,
        REJECTED,
        SUSPENDED
    }
}
