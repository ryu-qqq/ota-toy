package com.ryuqq.otatoy.domain.pricing;

public record RatePlanName(String value) {

    public RatePlanName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("요금 정책명은 필수입니다");
        }
    }

    public static RatePlanName of(String value) {
        return new RatePlanName(value);
    }
}
