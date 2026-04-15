package com.ryuqq.otatoy.application.pricing.port.out;

import com.ryuqq.otatoy.domain.pricing.RateRule;

/**
 * RateRule 저장 전용 Outbound Port.
 * Adapter(persistence-mysql)에서 구현한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public interface RateRuleCommandPort {

    /**
     * RateRule을 저장하고 할당된 ID를 반환한다.
     */
    Long persist(RateRule rateRule);
}
