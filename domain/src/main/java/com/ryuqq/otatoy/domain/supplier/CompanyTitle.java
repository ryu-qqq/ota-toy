package com.ryuqq.otatoy.domain.supplier;

/**
 * 회사명 VO. NOT NULL이며, 200자 이하를 보장한다.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public record CompanyTitle(String value) {

    private static final int MAX_LENGTH = 200;

    public CompanyTitle {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("회사명은 필수입니다");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("회사명은 " + MAX_LENGTH + "자 이하여야 합니다");
        }
    }

    public static CompanyTitle of(String value) {
        return new CompanyTitle(value);
    }
}
