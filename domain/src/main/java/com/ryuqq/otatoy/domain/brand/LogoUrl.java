package com.ryuqq.otatoy.domain.brand;

/**
 * 브랜드 로고 URL VO. nullable이며, 값이 있을 경우 500자 이하를 보장한다.
 */
public record LogoUrl(String value) {

    private static final int MAX_LENGTH = 500;

    public LogoUrl {
        if (value != null && value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("로고 URL은 " + MAX_LENGTH + "자 이하여야 합니다");
        }
    }

    public static LogoUrl of(String value) {
        return new LogoUrl(value);
    }
}
