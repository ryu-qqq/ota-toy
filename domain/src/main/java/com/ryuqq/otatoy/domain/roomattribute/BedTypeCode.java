package com.ryuqq.otatoy.domain.roomattribute;

public record BedTypeCode(String value) {

    private static final int MAX_LENGTH = 50;

    public BedTypeCode {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("침대 유형 코드는 필수입니다");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("침대 유형 코드는 " + MAX_LENGTH + "자 이하여야 합니다");
        }
    }

    public static BedTypeCode of(String value) {
        return new BedTypeCode(value);
    }
}
