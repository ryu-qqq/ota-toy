package com.ryuqq.otatoy.domain.pricing;

public record RateOverrideId(Long value) {

    public static RateOverrideId of(Long value) {
        return new RateOverrideId(value);
    }

    public static RateOverrideId forNew() { return new RateOverrideId(null); }

    public boolean isNew() {
        return value == null;
    }
}
