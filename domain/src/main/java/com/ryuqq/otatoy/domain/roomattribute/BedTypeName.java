package com.ryuqq.otatoy.domain.roomattribute;

/**
 * 침대 유형명. null/blank 불가, 최대 200자.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public record BedTypeName(String value) {

    private static final int MAX_LENGTH = 200;

    public BedTypeName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("침대 유형명은 필수입니다");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("침대 유형명은 " + MAX_LENGTH + "자 이하여야 합니다");
        }
    }

    public static BedTypeName of(String value) {
        return new BedTypeName(value);
    }
}
