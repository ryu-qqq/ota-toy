package com.ryuqq.otatoy.domain.accommodation;

public record ViewTypeCode(String value) {

    public ViewTypeCode {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("전망 유형 코드는 필수입니다");
        }
    }

    public static ViewTypeCode of(String value) {
        return new ViewTypeCode(value);
    }
}
