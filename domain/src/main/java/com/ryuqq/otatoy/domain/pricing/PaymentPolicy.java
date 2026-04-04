package com.ryuqq.otatoy.domain.pricing;

/**
 * 결제 정책.
 * PREPAY: 선결제, PAY_AT_PROPERTY: 현장결제, PAY_BEFORE_CHECKIN: 체크인 전 결제.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public enum PaymentPolicy {

    PREPAY("선결제"),
    PAY_AT_PROPERTY("현장결제"),
    PAY_BEFORE_CHECKIN("체크인 전 결제");

    private final String displayName;

    PaymentPolicy(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
