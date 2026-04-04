package com.ryuqq.otatoy.domain.pricing;

public record RatePlanAddOnId(Long value) {

    public static RatePlanAddOnId of(Long value) {
        return new RatePlanAddOnId(value);
    }

    public boolean isNew() {
        return value == null;
    }
}
