package com.ryuqq.otatoy.domain.pricing;

public record RateId(Long value) {

    public static RateId of(Long value) {
        return new RateId(value);
    }

    public boolean isNew() {
        return value == null;
    }
}
