package com.ryuqq.otatoy.domain.reservation;

/**
 * 투숙객 정보 VO. 이름(필수), 전화번호, 이메일을 포함한다.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public record GuestInfo(
        String name,
        String phone,
        String email
) {

    public GuestInfo {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("투숙객 이름은 필수입니다");
        }
    }

    public static GuestInfo of(String name, String phone, String email) {
        return new GuestInfo(name, phone, email);
    }
}
