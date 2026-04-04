package com.ryuqq.otatoy.domain.pricing;

/**
 * 요금 규칙 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public record RateRuleId(Long value) {

    public static RateRuleId of(Long value) {
        return new RateRuleId(value);
    }

    public static RateRuleId forNew() { return new RateRuleId(null); }

    public boolean isNew() {
        return value == null;
    }
}
