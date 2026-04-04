package com.ryuqq.otatoy.domain.partner;

public record PartnerId(Long value) {

    public PartnerId {
        if (value == null) {
            throw new IllegalArgumentException("파트너 ID는 필수입니다");
        }
    }

    public static PartnerId of(Long value) {
        return new PartnerId(value);
    }
}
