package com.ryuqq.otatoy.domain.accommodation;

public record PropertyAttributeValueId(Long value) {

    public static PropertyAttributeValueId of(Long value) {
        return new PropertyAttributeValueId(value);
    }

    public static PropertyAttributeValueId forNew() { return new PropertyAttributeValueId(null); }

    public boolean isNew() {
        return value == null;
    }
}
