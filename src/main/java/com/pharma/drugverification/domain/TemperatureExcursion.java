package com.pharma.drugverification.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "temperature_excursions", indexes = {
        @Index(name = "idx_batch_id", columnList = "batchId"),
        @Index(name = "idx_severity", columnList = "severity"),
        @Index(name = "idx_start_time", columnList = "startTime")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TemperatureExcursion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long batchId;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;

    @NotNull
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal minTemperature;

    @NotNull
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal maxTemperature;

    @Column(precision = 5, scale = 2)
    private BigDecimal thresholdMin;

    @Column(precision = 5, scale = 2)
    private BigDecimal thresholdMax;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Severity severity;

    @Column
    private Integer durationMinutes;

    @Column
    private Boolean resolved = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum Severity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}
