package com.ryuqq.otatoy.domain.reservation;

import com.ryuqq.otatoy.domain.common.vo.Email;
import com.ryuqq.otatoy.domain.common.vo.PhoneNumber;

/**
 * 투숙객 정보 VO. 이름(필수), 전화번호(선택), 이메일(선택)을 포함한다.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public record GuestInfo(
        String name,
        PhoneNumber phone,
        Email email
) {

    public GuestInfo {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("투숙객 이름은 필수입니다");
        }
    }

    public String phoneValue() {
        return phone != null ? phone.value() : null;
    }

    public String emailValue() {
        return email != null ? email.value() : null;
    }

    public static GuestInfo of(String name, String phone, String email) {
        return new GuestInfo(
                name,
                phone != null ? PhoneNumber.of(phone) : null,
                email != null ? Email.of(email) : null
        );
    }
}
