package com.ryuqq.otatoy.domain.common.vo;

import java.util.regex.Pattern;

public record PhoneNumber(String value) {

    private static final int MAX_LENGTH = 30;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9\\-]+$");

    public PhoneNumber {
        if (value != null) {
            if (value.isBlank()) {
                throw new IllegalArgumentException("전화번호가 비어있을 수 없습니다");
            }
            if (value.length() > MAX_LENGTH) {
                throw new IllegalArgumentException("전화번호는 " + MAX_LENGTH + "자 이하여야 합니다");
            }
            if (!PHONE_PATTERN.matcher(value).matches()) {
                throw new IllegalArgumentException("전화번호는 숫자와 하이픈만 포함할 수 있습니다: " + value);
            }
        }
    }

    public static PhoneNumber of(String value) {
        return new PhoneNumber(value);
    }
}
