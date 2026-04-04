package com.ryuqq.otatoy.domain.pricing;

/**
 * 요금 정책 부가 서비스 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 */
public record RatePlanAddOnId(Long value) {

    public static RatePlanAddOnId of(Long value) {
        return new RatePlanAddOnId(value);
    }

    public static RatePlanAddOnId forNew() { return new RatePlanAddOnId(null); }

    public boolean isNew() {
        return value == null;
    }
}
