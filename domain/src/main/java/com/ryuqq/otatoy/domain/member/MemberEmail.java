package com.ryuqq.otatoy.domain.member;

/**
 * 회원 이메일. 로그인 ID로 사용된다. null/blank 불가, '@' 포함 필수, 최대 200자.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public record MemberEmail(String value) {

    private static final int MAX_LENGTH = 200;

    public MemberEmail {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("이메일은 " + MAX_LENGTH + "자 이하여야 합니다");
        }
        if (!value.contains("@")) {
            throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다");
        }
    }

    public static MemberEmail of(String value) {
        return new MemberEmail(value);
    }
}
