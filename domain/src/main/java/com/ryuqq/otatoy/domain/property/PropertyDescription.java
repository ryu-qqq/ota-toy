package com.ryuqq.otatoy.domain.property;

public record PropertyDescription(String value) {

    private static final int MAX_LENGTH = 2000;

    public PropertyDescription {
        if (value != null && value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("숙소 설명은 " + MAX_LENGTH + "자 이하여야 합니다");
        }
    }

    public static PropertyDescription of(String value) {
        return new PropertyDescription(value);
    }
}
