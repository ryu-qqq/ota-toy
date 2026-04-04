package com.ryuqq.otatoy.domain.pricing;

public record RateRuleId(Long value) {

    public static RateRuleId of(Long value) {
        return new RateRuleId(value);
    }

    public boolean isNew() {
        return value == null;
    }
}
