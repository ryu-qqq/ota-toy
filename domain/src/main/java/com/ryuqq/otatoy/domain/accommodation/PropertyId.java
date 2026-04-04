package com.ryuqq.otatoy.domain.accommodation;

public record PropertyId(Long value) {

    public static PropertyId of(Long value) {
        return new PropertyId(value);
    }

    public boolean isNew() {
        return value == null;
    }
}
