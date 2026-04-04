package com.ryuqq.otatoy.domain.common.vo;

public record PhoneNumber(String value) {

    public static PhoneNumber of(String value) {
        return new PhoneNumber(value);
    }
}
