package com.ryuqq.otatoy.domain.accommodation;

public record PropertyTypeDescription(String value) {

    private static final int MAX_LENGTH = 2000;

    public PropertyTypeDescription {
        if (value != null && value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("숙소 유형 설명은 " + MAX_LENGTH + "자 이하여야 합니다");
        }
    }

    public static PropertyTypeDescription of(String value) {
        return new PropertyTypeDescription(value);
    }
}
