package com.ryuqq.otatoy.domain.pricing;

/**
 * 요금 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 */
public record RateId(Long value) {

    public static RateId of(Long value) {
        return new RateId(value);
    }

    public static RateId forNew() { return new RateId(null); }

    public boolean isNew() {
        return value == null;
    }
}
