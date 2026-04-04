package com.ryuqq.otatoy.domain.partner;

/**
 * 파트너명. null/blank 불가, 최대 200자.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public record PartnerName(String value) {

    private static final int MAX_LENGTH = 200;

    public PartnerName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("파트너명은 필수입니다");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("파트너명은 " + MAX_LENGTH + "자 이하여야 합니다");
        }
    }

    public static PartnerName of(String value) {
        return new PartnerName(value);
    }
}
