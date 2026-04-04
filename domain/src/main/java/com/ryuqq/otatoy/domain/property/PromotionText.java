package com.ryuqq.otatoy.domain.property;

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
