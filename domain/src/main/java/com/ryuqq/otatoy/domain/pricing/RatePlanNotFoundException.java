package com.ryuqq.otatoy.domain.pricing;

import com.ryuqq.otatoy.domain.common.DomainException;

/**
 * 요금 정책을 찾을 수 없을 때 발생하는 예외.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public class RatePlanNotFoundException extends DomainException {

    public RatePlanNotFoundException() {
        super(PricingErrorCode.RATE_PLAN_NOT_FOUND);
    }
}
