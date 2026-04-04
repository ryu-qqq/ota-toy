package com.ryuqq.otatoy.domain.partner;

public record MemberName(String value) {

    public MemberName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("멤버 이름은 필수입니다");
        }
    }

    public static MemberName of(String value) {
        return new MemberName(value);
    }
}
