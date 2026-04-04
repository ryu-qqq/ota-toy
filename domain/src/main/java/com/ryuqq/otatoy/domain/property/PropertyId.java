package com.ryuqq.otatoy.domain.property;

public record PropertyId(Long value) {

    public static PropertyId of(Long value) {
        return new PropertyId(value);
    }

    public static PropertyId forNew() { return new PropertyId(null); }

    public boolean isNew() {
        return value == null;
    }
}
