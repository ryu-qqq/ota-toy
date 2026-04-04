package com.ryuqq.otatoy.domain.pricing;

public record RatePlanId(Long value) {

    public static RatePlanId of(Long value) {
        return new RatePlanId(value);
    }

    public static RatePlanId forNew() { return new RatePlanId(null); }

    public boolean isNew() {
        return value == null;
    }
}
