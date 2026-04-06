package com.ryuqq.otatoy.persistence.redis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Rate 캐시 설정 프로퍼티.
 * TTL, Key Prefix 등을 외부 설정으로 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ConfigurationProperties(prefix = "rate-cache")
public class RateCacheProperties {

    private String keyPrefix = "rate:";
    private Duration ttl = Duration.ofHours(24);

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public Duration getTtl() {
        return ttl;
    }

    public void setTtl(Duration ttl) {
        this.ttl = ttl;
    }

    public long getTtlMillis() {
        return ttl.toMillis();
    }
}
