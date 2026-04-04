package com.ryuqq.otatoy.domain.pricing;

/**
 * 요금 정책 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 */
public record RatePlanId(Long value) {

    public static RatePlanId of(Long value) {
        return new RatePlanId(value);
    }

    public static RatePlanId forNew() { return new RatePlanId(null); }

    public boolean isNew() {
        return value == null;
    }
}
