package com.pharma.drugverification.config;

import com.pharma.drugverification.service.IdempotencyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class IdempotencyFilter extends OncePerRequestFilter {

    private final IdempotencyService idempotencyService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String idempotencyKey = request.getHeader("Idempotency-Key");

        // Only apply idempotency for mutating methods (POST, PUT, PATCH, DELETE)
        boolean isMutating = isMutatingMethod(request.getMethod());

        if (idempotencyKey == null || !isMutating) {
            filterChain.doFilter(request, response);
            return;
        }

        if (idempotencyService.containsKey(idempotencyKey)) {
            IdempotencyService.CachedResponse cachedResponse = idempotencyService.getResponse(idempotencyKey);
            if (cachedResponse != null) {
                returnCachedResponse(response, cachedResponse);
                return;
            }
        }

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, responseWrapper);

        // Only cache successful responses (2xx) or as per business requirement.
        // For now, caching everything to ensure exactly-once execution result is
        // consistent.
        // Actually, typically we only cache successful idempotent operations.
        // If it failed with 500, we might want to allow retry.
        // Let's cache 2xx and 4xx (client errors should be consistently returned). 5xx
        // might be retriable.
        if (responseWrapper.getStatus() < 500) {
            cacheResponse(idempotencyKey, responseWrapper);
        }

        responseWrapper.copyBodyToResponse();
    }

    private boolean isMutatingMethod(String method) {
        return "POST".equalsIgnoreCase(method) ||
                "PUT".equalsIgnoreCase(method) ||
                "PATCH".equalsIgnoreCase(method) ||
                "DELETE".equalsIgnoreCase(method);
    }

    private void returnCachedResponse(HttpServletResponse response, IdempotencyService.CachedResponse cachedResponse)
            throws IOException {
        response.setStatus(cachedResponse.getStatus());
        if (cachedResponse.getHeaders() != null) {
            cachedResponse.getHeaders().forEach(response::setHeader);
        }
        response.getWriter().write(cachedResponse.getBody());
        // Start idempotency key header to indicate it was a cached response
        response.setHeader("X-Idempotency-Hit", "true");
    }

    private void cacheResponse(String key, ContentCachingResponseWrapper responseWrapper) throws IOException {
        Map<String, String> headers = new HashMap<>();
        for (String headerName : responseWrapper.getHeaderNames()) {
            headers.put(headerName, responseWrapper.getHeader(headerName));
        }

        String body = new String(responseWrapper.getContentAsByteArray(), responseWrapper.getCharacterEncoding());

        IdempotencyService.CachedResponse cachedResponse = IdempotencyService.CachedResponse.builder()
                .status(responseWrapper.getStatus())
                .headers(headers)
                .body(body)
                .build();

        idempotencyService.saveResponse(key, cachedResponse);
    }
}
