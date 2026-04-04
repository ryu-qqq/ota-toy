package com.ryuqq.otatoy.domain.roomattribute;

/**
 * 전망 유형 코드. null/blank 불가, 최대 50자.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public record ViewTypeCode(String value) {

    private static final int MAX_LENGTH = 50;

    public ViewTypeCode {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("전망 유형 코드는 필수입니다");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("전망 유형 코드는 " + MAX_LENGTH + "자 이하여야 합니다");
        }
    }

    public static ViewTypeCode of(String value) {
        return new ViewTypeCode(value);
    }
}
