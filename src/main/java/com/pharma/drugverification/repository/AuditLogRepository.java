package com.pharma.drugverification.repository;

import com.pharma.drugverification.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

        Page<AuditLog> findByUserId(Long userId, Pageable pageable);

        Page<AuditLog> findByAction(String action, Pageable pageable);

        Page<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);

        @Query("SELECT al FROM AuditLog al WHERE al.createdAt BETWEEN :startTime AND :endTime ORDER BY al.createdAt")
        List<AuditLog> findByTimeRange(@Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        @Query("SELECT al FROM AuditLog al WHERE al.blockchainTransactionId IS NULL ORDER BY al.createdAt")
        List<AuditLog> findUnanchoredLogs(Pageable pageable);

        Optional<AuditLog> findTopByOrderByIdDesc();

        Optional<AuditLog> findByPreviousHash(String previousHash);
}
