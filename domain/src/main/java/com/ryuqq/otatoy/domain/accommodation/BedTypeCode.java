package com.ryuqq.otatoy.domain.accommodation;

public record BedTypeCode(String value) {

    public BedTypeCode {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("침대 유형 코드는 필수입니다");
        }
    }

    public static BedTypeCode of(String value) {
        return new BedTypeCode(value);
    }
}
