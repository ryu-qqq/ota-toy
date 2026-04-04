package com.ryuqq.otatoy.domain.pricing;

/**
 * 요금 오버라이드 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 */
public record RateOverrideId(Long value) {

    public static RateOverrideId of(Long value) {
        return new RateOverrideId(value);
    }

    public static RateOverrideId forNew() { return new RateOverrideId(null); }

    public boolean isNew() {
        return value == null;
    }
}
