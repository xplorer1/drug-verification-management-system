package com.pharma.drugverification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final long EXPIRATION_HOURS = 24;

    public boolean containsKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("idempotency:" + key));
    }

    public CachedResponse getResponse(String key) {
        String json = redisTemplate.opsForValue().get("idempotency:" + key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, CachedResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize cached response", e);
            return null;
        }
    }

    public void saveResponse(String key, CachedResponse response) {
        try {
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(
                    "idempotency:" + key,
                    json,
                    EXPIRATION_HOURS,
                    TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize cached response", e);
        }
    }

    @Data
    @Builder
    public static class CachedResponse {
        private int status;
        private Map<String, String> headers;
        private String body;
    }
}
