package com.ryuqq.otatoy.application.pricing.port.out;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Rate 캐시 전용 Outbound Port (Redis).
 * Write-Through 전략: 쓸 때 Redis SET, 읽을 때 Redis MGET.
 * adapter-out/persistence-redis에서 구현한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface RateCachePort {

    /**
     * 여러 RatePlan의 여러 날짜 요금을 일괄 조회한다 (Redis MGET 1회).
     * 캐시 히트한 항목만 반환한다.
     *
     * @param ratePlanIds 요금 정책 ID 목록
     * @param dates 조회할 날짜 목록
     * @return "ratePlanId:date" → price 매핑 (캐시 히트한 것만)
     */
    Map<String, BigDecimal> multiGet(List<Long> ratePlanIds, List<LocalDate> dates);

    /**
     * 여러 Rate를 일괄 저장한다 (Redis MSET 1회, Write-Through).
     * TTL은 구현체에서 설정한다.
     *
     * @param rates "ratePlanId:date" → price 매핑
     */
    void multiSet(Map<String, BigDecimal> rates);
}
