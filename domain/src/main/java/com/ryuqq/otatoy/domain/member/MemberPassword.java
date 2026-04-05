package com.ryuqq.otatoy.domain.member;

/**
 * 회원 비밀번호. 해시된 값을 저장한다. null/blank 불가, 최대 500자.
 * 실제 해싱은 Application/Adapter 레이어에서 수행하며, 도메인은 해시값만 보관한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public record MemberPassword(String hashedValue) {

    private static final int MAX_LENGTH = 500;

    public MemberPassword {
        if (hashedValue == null || hashedValue.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다");
        }
        if (hashedValue.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("비밀번호 해시는 " + MAX_LENGTH + "자 이하여야 합니다");
        }
    }

    public static MemberPassword of(String hashedValue) {
        return new MemberPassword(hashedValue);
    }
}
