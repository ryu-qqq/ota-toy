package com.ryuqq.otatoy.application.pricing.port.out;

import com.ryuqq.otatoy.domain.pricing.RatePlan;

/**
 * RatePlan 저장 전용 Outbound Port.
 * Adapter(persistence-mysql)에서 구현한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public interface RatePlanCommandPort {

    Long persist(RatePlan ratePlan);
}
