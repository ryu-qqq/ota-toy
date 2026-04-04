package com.ryuqq.otatoy.domain.pricing;

public record RateOverrideId(Long value) {

    public static RateOverrideId of(Long value) {
        return new RateOverrideId(value);
    }

    public boolean isNew() {
        return value == null;
    }
}
