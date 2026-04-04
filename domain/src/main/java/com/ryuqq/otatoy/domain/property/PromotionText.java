package com.ryuqq.otatoy.domain.property;

/**
 * 숙소 홍보 문구. nullable이며, 값이 있을 경우 최대 500자.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public record PromotionText(String value) {

    private static final int MAX_LENGTH = 500;

    public PromotionText {
        if (value != null && value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("홍보 문구는 " + MAX_LENGTH + "자 이하여야 합니다");
        }
    }

    public static PromotionText of(String value) {
        return new PromotionText(value);
    }
}
