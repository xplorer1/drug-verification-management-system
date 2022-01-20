package com.pharma.drugverification.config;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
public class CacheConfiguration implements CachingConfigurer {

        @Bean
        public RedisCacheConfiguration redisCacheConfiguration() {
                return RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofHours(1))
                                .disableCachingNullValues()
                                .serializeValuesWith(
                                                RedisSerializationContext.SerializationPair.fromSerializer(
                                                                new GenericJackson2JsonRedisSerializer()));
        }

        @Bean
        public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
                return builder -> builder
                                .withCacheConfiguration("drugDetails",
                                                RedisCacheConfiguration.defaultCacheConfig()
                                                                .entryTtl(Duration.ofHours(1)))
                                .withCacheConfiguration("verificationResults",
                                                RedisCacheConfiguration.defaultCacheConfig()
                                                                .entryTtl(Duration.ofMinutes(5)))
                                .withCacheConfiguration("batchDetails",
                                                RedisCacheConfiguration.defaultCacheConfig()
                                                                .entryTtl(Duration.ofHours(1)));
        }
}
