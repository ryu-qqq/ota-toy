package com.ryuqq.otatoy.domain.common.vo;

import java.math.BigDecimal;

public record Money(BigDecimal amount) {

    public Money {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("금액은 0 이상이어야 합니다");
        }
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }

    public static Money of(int amount) {
        return new Money(BigDecimal.valueOf(amount));
    }

    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    public Money multiply(int factor) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(factor)));
    }

    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }
}
