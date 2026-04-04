package com.ryuqq.otatoy.domain.pricing;

public record RatePlanId(Long value) {

    public static RatePlanId of(Long value) {
        return new RatePlanId(value);
    }

    public boolean isNew() {
        return value == null;
    }
}
