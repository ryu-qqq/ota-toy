package com.ryuqq.otatoy.domain.member;

/**
 * 회원 이름. null/blank 불가, 최대 100자.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public record MemberName(String value) {

    private static final int MAX_LENGTH = 100;

    public MemberName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("회원 이름은 필수입니다");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("회원 이름은 " + MAX_LENGTH + "자 이하여야 합니다");
        }
    }

    public static MemberName of(String value) {
        return new MemberName(value);
    }
}
