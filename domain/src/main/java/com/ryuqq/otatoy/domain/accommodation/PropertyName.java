package com.ryuqq.otatoy.domain.accommodation;

public record PropertyName(String value) {

    private static final int MAX_LENGTH = 100;

    public PropertyName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("숙소명은 필수입니다");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("숙소명은 " + MAX_LENGTH + "자 이하여야 합니다");
        }
    }

    public static PropertyName of(String value) {
        return new PropertyName(value);
    }
}
