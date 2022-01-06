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

import java.time.LocalDateTime;

@Entity
@Table(name = "serialized_units", indexes = {
        @Index(name = "idx_serial_number", columnList = "serialNumber", unique = true),
        @Index(name = "idx_batch_id", columnList = "batchId"),
        @Index(name = "idx_gtin", columnList = "gtin"),
        @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SerializedUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 50)
    private String serialNumber;

    @NotNull
    @Column(nullable = false)
    private Long batchId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batchId", insertable = false, updatable = false)
    private Batch batch;

    @NotBlank
    @Column(nullable = false, length = 14)
    private String gtin;

    @NotBlank
    @Column(nullable = false, length = 512)
    private String cryptoTail;

    @NotBlank
    @Column(nullable = false, length = 1000)
    private String dataMatrix;

    @NotNull
    @Column(nullable = false)
    private Integer keyVersion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UnitStatus status = UnitStatus.ACTIVE;

    @Column
    private Long parentAggregationId;

    @Column
    private LocalDateTime dispensedAt;

    @Column
    private Long dispensedByUserId;

    @Column
    private String dispensedByPharmacy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum UnitStatus {
        ACTIVE,
        IN_TRANSIT,
        DISPENSED,
        QUARANTINED,
        RECALLED,
        DESTROYED
    }
}
