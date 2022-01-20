package com.pharma.drugverification.config;

import com.pharma.drugverification.service.IdempotencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class IdempotencyFilterTest {

    private IdempotencyService idempotencyService;
    private IdempotencyFilter idempotencyFilter;

    @BeforeEach
    void setUp() {
        idempotencyService = mock(IdempotencyService.class);
        idempotencyFilter = new IdempotencyFilter(idempotencyService);
    }

    @Test
    void doFilterInternal_ShouldProceed_WhenNoIdempotencyKey() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        idempotencyFilter.doFilterInternal(request, response, filterChain);

        verify(idempotencyService, never()).containsKey(any());
        verify(idempotencyService, never()).saveResponse(any(), any());
    }

    @Test
    void doFilterInternal_ShouldReturnCachedResponse_WhenKeyExists() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.addHeader("Idempotency-Key", "test-key");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        IdempotencyService.CachedResponse cachedResponse = IdempotencyService.CachedResponse.builder()
                .status(201)
                .body("cached-body")
                .headers(Map.of("Content-Type", "application/json"))
                .build();

        when(idempotencyService.containsKey("test-key")).thenReturn(true);
        when(idempotencyService.getResponse("test-key")).thenReturn(cachedResponse);

        idempotencyFilter.doFilterInternal(request, response, filterChain);

        assertEquals(201, response.getStatus());
        assertEquals("cached-body", response.getContentAsString());
        assertEquals("application/json", response.getHeader("Content-Type"));
        assertEquals("true", response.getHeader("X-Idempotency-Hit"));
    }

    @Test
    void doFilterInternal_ShouldCacheResponse_WhenKeyDoesNotExist() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.addHeader("Idempotency-Key", "new-key");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(idempotencyService.containsKey("new-key")).thenReturn(false);

        idempotencyFilter.doFilterInternal(request, response, filterChain);

        verify(idempotencyService, times(1)).saveResponse(eq("new-key"), any(IdempotencyService.CachedResponse.class));
    }
}
