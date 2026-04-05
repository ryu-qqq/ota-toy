package com.ryuqq.otatoy.application.pricing.manager;

import com.ryuqq.otatoy.application.pricing.port.out.RatePlanCommandPort;
import com.ryuqq.otatoy.domain.pricing.RatePlan;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * RatePlan 저장 트랜잭션 경계 관리자.
 * 단일 Aggregate 저장을 담당한다 (APP-MGR-001).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class RatePlanCommandManager {

    private final RatePlanCommandPort ratePlanCommandPort;

    public RatePlanCommandManager(RatePlanCommandPort ratePlanCommandPort) {
        this.ratePlanCommandPort = ratePlanCommandPort;
    }

    @Transactional
    public Long persist(RatePlan ratePlan) {
        return ratePlanCommandPort.persist(ratePlan);
    }
}
