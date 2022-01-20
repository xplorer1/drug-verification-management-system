package com.pharma.drugverification.config;

import com.pharma.drugverification.service.AuditService;
import com.pharma.drugverification.service.StatusTransitionService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        return Mockito.mock(RedisConnectionFactory.class);
    }

    @Bean
    @Primary
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        return Mockito.mock(RedisTemplate.class);
    }

    @Bean
    @Primary
    public AuditService auditService() {
        return Mockito.mock(AuditService.class);
    }

    @Bean
    @Primary
    public StatusTransitionService statusTransitionService() {
        return Mockito.mock(StatusTransitionService.class);
    }
}
