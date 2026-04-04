package com.ryuqq.otatoy.domain.accommodation;

public record PropertyTypeId(Long value) {

    public static PropertyTypeId of(Long value) {
        return new PropertyTypeId(value);
    }

    public boolean isNew() {
        return value == null;
    }
}
