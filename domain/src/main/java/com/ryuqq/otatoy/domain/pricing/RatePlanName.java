package com.ryuqq.otatoy.domain.pricing;

public record RatePlanName(String value) {

    private static final int MAX_LENGTH = 200;

    public RatePlanName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("요금 정책명은 필수입니다");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("요금 정책명은 " + MAX_LENGTH + "자 이하여야 합니다");
        }
    }

    public static RatePlanName of(String value) {
        return new RatePlanName(value);
    }
}
