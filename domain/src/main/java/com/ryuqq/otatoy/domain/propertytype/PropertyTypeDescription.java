package com.ryuqq.otatoy.domain.propertytype;

/**
 * 숙소 유형 설명. nullable이며, 값이 있을 경우 최대 2000자.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
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
