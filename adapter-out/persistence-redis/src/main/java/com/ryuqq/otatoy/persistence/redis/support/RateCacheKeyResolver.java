package com.ryuqq.otatoy.persistence.redis.support;

import com.ryuqq.otatoy.persistence.redis.config.RateCacheProperties;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Rate 캐시 키 생성 전담 클래스.
 * 키 형식: {prefix}{ratePlanId}:{date}
 * 예시: rate:42:2026-04-10
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class RateCacheKeyResolver {

    private final String keyPrefix;

    public RateCacheKeyResolver(RateCacheProperties properties) {
        this.keyPrefix = properties.getKeyPrefix();
    }

    /**
     * Redis 키 생성.
     */
    public String resolve(Long ratePlanId, LocalDate date) {
        return keyPrefix + ratePlanId + ":" + date;
    }

    /**
     * 여러 ratePlanId × 날짜 조합의 Redis 키 목록 생성.
     */
    public List<String> resolveAll(List<Long> ratePlanIds, List<LocalDate> dates) {
        return ratePlanIds.stream()
                .flatMap(id -> dates.stream().map(date -> resolve(id, date)))
                .toList();
    }

    /**
     * 캐시 결과 키 생성 (Redis 키가 아닌 Application 레이어용).
     * 형식: {ratePlanId}:{date}
     */
    public String resultKey(Long ratePlanId, LocalDate date) {
        return ratePlanId + ":" + date;
    }

    /**
     * Redis 키에서 결과 키 추출.
     * "rate:42:2026-04-10" → "42:2026-04-10"
     */
    public String toResultKey(String redisKey) {
        return redisKey.substring(keyPrefix.length());
    }
}
