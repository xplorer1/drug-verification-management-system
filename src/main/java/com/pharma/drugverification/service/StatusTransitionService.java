package com.pharma.drugverification.service;

import com.pharma.drugverification.domain.StatusTransition;
import com.pharma.drugverification.domain.User;
import com.pharma.drugverification.repository.StatusTransitionRepository;
import com.pharma.drugverification.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatusTransitionService {

    private final StatusTransitionRepository statusTransitionRepository;
    private final UserRepository userRepository;

    @Transactional
    public void recordTransition(
            StatusTransition.EntityType entityType,
            Long entityId,
            String fromStatus,
            String toStatus,
            String reason,
            Long userId) {
        User user = userRepository.findById(userId).orElse(null);

        StatusTransition transition = new StatusTransition();
        transition.setEntityType(entityType);
        transition.setEntityId(entityId);
        transition.setFromStatus(fromStatus);
        transition.setToStatus(toStatus);
        transition.setReason(reason);
        transition.setChangedByUserId(userId);
        transition.setChangedByUsername(user != null ? user.getUsername() : "unknown");

        statusTransitionRepository.save(transition);
        log.info("Recorded status transition for {} {}: {} -> {}", entityType, entityId, fromStatus, toStatus);
    }

    @Transactional(readOnly = true)
    public List<StatusTransition> getTransitionHistory(StatusTransition.EntityType entityType, Long entityId) {
        return statusTransitionRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    @Transactional(readOnly = true)
    public Page<StatusTransition> getTransitionHistoryPaged(
            StatusTransition.EntityType entityType,
            Long entityId,
            Pageable pageable) {
        return statusTransitionRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable);
    }
}
