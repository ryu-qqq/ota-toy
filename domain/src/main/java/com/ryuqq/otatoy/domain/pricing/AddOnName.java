package com.ryuqq.otatoy.domain.pricing;

/**
 * 부가 서비스(Add-on) 이름. null/blank 불가, 최대 200자.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public record AddOnName(String value) {

    private static final int MAX_LENGTH = 200;

    public AddOnName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Add-on 이름은 필수입니다");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Add-on 이름은 " + MAX_LENGTH + "자 이하여야 합니다");
        }
    }

    public static AddOnName of(String value) {
        return new AddOnName(value);
    }
}
