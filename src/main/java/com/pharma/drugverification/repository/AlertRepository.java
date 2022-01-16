package com.pharma.drugverification.repository;

import com.pharma.drugverification.domain.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    Page<Alert> findByAcknowledgedFalse(Pageable pageable);

    Page<Alert> findByAlertType(Alert.AlertType alertType, Pageable pageable);

    Page<Alert> findBySeverity(Alert.AlertSeverity severity, Pageable pageable);

    List<Alert> findByRelatedEntityTypeAndRelatedEntityId(String relatedEntityType, Long relatedEntityId);

    long countByAcknowledgedFalseAndSeverity(Alert.AlertSeverity severity);

    long countByAlertTypeAndCreatedAtAfter(Alert.AlertType alertType, LocalDateTime since);

    Page<Alert> findByRelatedEntityTypeAndRelatedEntityId(String relatedEntityType, Long relatedEntityId,
            Pageable pageable);

    List<Alert> findByAcknowledged(Boolean acknowledged);

    List<Alert> findByCreatedAtAfter(LocalDateTime createdAt);
}
