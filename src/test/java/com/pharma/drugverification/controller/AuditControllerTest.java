package com.pharma.drugverification.controller;

import com.pharma.drugverification.dto.AuditLogResponse;
import com.pharma.drugverification.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditControllerTest {

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuditController auditController;

    private AuditLogResponse logResponse;

    @BeforeEach
    void setUp() {
        logResponse = new AuditLogResponse();
        logResponse.setId(1L);
        logResponse.setAction("CREATE_BATCH");
        logResponse.setEntityType("Batch");
        logResponse.setEntityId(10L);
        logResponse.setUserId(1L);
        logResponse.setUsername("admin");
        logResponse.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getLogs_Success() {
        Page<AuditLogResponse> page = new PageImpl<>(List.of(logResponse));
        when(auditService.getLogs(any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<AuditLogResponse>> response = auditController.getLogs(Pageable.unpaged());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals("CREATE_BATCH", response.getBody().getContent().get(0).getAction());
        verify(auditService).getLogs(any(Pageable.class));
    }

    @Test
    void getLogsByEntity_Success() {
        Page<AuditLogResponse> page = new PageImpl<>(List.of(logResponse));
        when(auditService.getLogsByEntity(eq("Batch"), eq(10L), any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<AuditLogResponse>> response = auditController.getLogsByEntity("Batch", 10L,
                Pageable.unpaged());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Batch", response.getBody().getContent().get(0).getEntityType());
        verify(auditService).getLogsByEntity(eq("Batch"), eq(10L), any(Pageable.class));
    }

    @Test
    void getLogsByUser_Success() {
        Page<AuditLogResponse> page = new PageImpl<>(List.of(logResponse));
        when(auditService.getLogsByUser(eq(1L), any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<AuditLogResponse>> response = auditController.getLogsByUser(1L, Pageable.unpaged());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getContent().get(0).getUserId());
        verify(auditService).getLogsByUser(eq(1L), any(Pageable.class));
    }
}
