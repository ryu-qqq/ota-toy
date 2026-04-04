package com.ryuqq.otatoy.domain.accommodation;

public record PropertyTypeName(String value) {

    public PropertyTypeName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("숙소 유형명은 필수입니다");
        }
    }

    public static PropertyTypeName of(String value) {
        return new PropertyTypeName(value);
    }
}
