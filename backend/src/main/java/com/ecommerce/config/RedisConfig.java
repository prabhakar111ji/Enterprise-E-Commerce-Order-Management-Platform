package com.ecommerce.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

/**
 * Redis Cache Configuration.
 *
 * WHY Redis for caching?
 * - In-memory store → sub-millisecond read latency
 * - Reduces database load for frequently accessed data (e.g., product listings, carts)
 * - Supports TTL (Time To Live) → stale data is automatically evicted
 *
 * WHAT happens if Redis is down?
 * - Application still works — falls back to PostgreSQL directly
 * - Spring's cache abstraction handles this gracefully
 *
 * ALTERNATIVE: In-process cache (Caffeine) — faster but not shared across instances.
 * TRADE-OFF: Redis adds network hop but enables shared cache in multi-instance deployments.
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
