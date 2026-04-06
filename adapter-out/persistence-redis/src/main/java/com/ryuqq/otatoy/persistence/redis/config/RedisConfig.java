package com.ryuqq.otatoy.persistence.redis.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Redis 관련 설정 활성화.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Configuration
@EnableConfigurationProperties(RateCacheProperties.class)
public class RedisConfig {
}
