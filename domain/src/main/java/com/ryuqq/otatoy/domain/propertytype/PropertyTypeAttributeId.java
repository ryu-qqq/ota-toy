package com.ryuqq.otatoy.domain.propertytype;

public record PropertyTypeAttributeId(Long value) {

    public static PropertyTypeAttributeId of(Long value) {
        return new PropertyTypeAttributeId(value);
    }

    public static PropertyTypeAttributeId forNew() { return new PropertyTypeAttributeId(null); }

    public boolean isNew() {
        return value == null;
    }
}
