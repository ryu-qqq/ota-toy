package com.ryuqq.otatoy.application.pricing.manager;

import com.ryuqq.otatoy.application.pricing.port.out.RateQueryPort;
import com.ryuqq.otatoy.domain.pricing.Rate;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Rate 조회 트랜잭션 경계 관리자.
 * 읽기 전용 트랜잭션으로 조회를 담당한다 (APP-MGR-001).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class RateReadManager {

    private final RateQueryPort rateQueryPort;

    public RateReadManager(RateQueryPort rateQueryPort) {
        this.rateQueryPort = rateQueryPort;
    }

    /**
     * 특정 요금 정책들의 날짜 범위에 해당하는 요금 목록을 조회한다.
     */
    @Transactional(readOnly = true)
    public List<Rate> findByRatePlanIdsAndDateRange(List<RatePlanId> ratePlanIds,
                                                     LocalDate startDate,
                                                     LocalDate endDate) {
        return rateQueryPort.findByRatePlanIdsAndDateRange(ratePlanIds, startDate, endDate);
    }
}
