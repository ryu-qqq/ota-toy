package com.ryuqq.otatoy.domain.accommodation;

public record PropertyTypeAttributeId(Long value) {

    public static PropertyTypeAttributeId of(Long value) {
        return new PropertyTypeAttributeId(value);
    }

    public boolean isNew() {
        return value == null;
    }
}
