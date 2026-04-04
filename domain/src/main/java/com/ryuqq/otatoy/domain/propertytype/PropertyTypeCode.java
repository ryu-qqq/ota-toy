package com.ryuqq.otatoy.domain.propertytype;

public record PropertyTypeCode(String value) {

    private static final int MAX_LENGTH = 50;

    public PropertyTypeCode {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("숙소 유형 코드는 필수입니다");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("숙소 유형 코드는 " + MAX_LENGTH + "자 이하여야 합니다");
        }
    }

    public static PropertyTypeCode of(String value) {
        return new PropertyTypeCode(value);
    }
}
