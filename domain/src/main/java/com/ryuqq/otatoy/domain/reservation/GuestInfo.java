package com.ryuqq.otatoy.domain.reservation;

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
