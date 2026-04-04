package com.ryuqq.otatoy.domain.propertytype;

/**
 * 숙소 유형명. null/blank 불가, 최대 200자.
 */
public record PropertyTypeName(String value) {

    private static final int MAX_LENGTH = 200;

    public PropertyTypeName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("숙소 유형명은 필수입니다");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("숙소 유형명은 " + MAX_LENGTH + "자 이하여야 합니다");
        }
    }

    public static PropertyTypeName of(String value) {
        return new PropertyTypeName(value);
    }
}
