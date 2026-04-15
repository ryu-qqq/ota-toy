package com.ryuqq.otatoy.application.pricing.port.out;

import com.ryuqq.otatoy.domain.pricing.Rate;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;

import java.time.LocalDate;
import java.util.List;

/**
 * Rate 조회 전용 Outbound Port.
 * Adapter(persistence-mysql)에서 구현한다 (APP-PRT-001).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface RateQueryPort {

    /**
     * 특정 요금 정책의 날짜 범위에 해당하는 요금 목록을 조회한다.
     *
     * @param ratePlanIds 요금 정책 ID 목록
     * @param startDate 시작 날짜 (포함)
     * @param endDate 종료 날짜 (미포함)
     * @return 날짜별 요금 목록
     */
    List<Rate> findByRatePlanIdsAndDateRange(List<RatePlanId> ratePlanIds, LocalDate startDate, LocalDate endDate);
}
