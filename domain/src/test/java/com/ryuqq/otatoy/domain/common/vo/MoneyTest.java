package com.ryuqq.otatoy.domain.common.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("BigDecimal로 생성할 수 있다")
        void shouldCreateWithBigDecimal() {
            Money money = Money.of(BigDecimal.valueOf(10000));
            assertThat(money.amount()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        }

        @Test
        @DisplayName("int로 생성할 수 있다")
        void shouldCreateWithInt() {
            Money money = Money.of(100);
            assertThat(money.amount()).isEqualByComparingTo(BigDecimal.valueOf(100));
        }

        @Test
        @DisplayName("0원으로 생성할 수 있다")
        void shouldCreateWithZero() {
            Money money = Money.of(0);
            assertThat(money.isZero()).isTrue();
        }

        @Test
        @DisplayName("null이면 예외가 발생한다")
        void shouldThrowWhenNull() {
            assertThatThrownBy(() -> Money.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("금액은 0 이상");
        }

        @Test
        @DisplayName("음수이면 예외가 발생한다")
        void shouldThrowWhenNegative() {
            assertThatThrownBy(() -> Money.of(BigDecimal.valueOf(-1)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("금액은 0 이상");
        }
    }

    @Nested
    @DisplayName("연산 검증")
    class Operations {

        @Test
        @DisplayName("두 금액을 더할 수 있다")
        void shouldAdd() {
            Money a = Money.of(10000);
            Money b = Money.of(5000);

            Money result = a.add(b);

            assertThat(result.amount()).isEqualByComparingTo(BigDecimal.valueOf(15000));
        }

        @Test
        @DisplayName("정수 배수로 곱할 수 있다")
        void shouldMultiply() {
            Money money = Money.of(10000);

            Money result = money.multiply(3);

            assertThat(result.amount()).isEqualByComparingTo(BigDecimal.valueOf(30000));
        }

        @Test
        @DisplayName("0을 곱하면 0원이 된다")
        void shouldMultiplyByZero() {
            Money money = Money.of(10000);

            Money result = money.multiply(0);

            assertThat(result.isZero()).isTrue();
        }
    }

    @Nested
    @DisplayName("비교 검증")
    class Comparison {

        @Test
        @DisplayName("큰 금액을 비교할 수 있다")
        void shouldCompareGreaterThan() {
            Money a = Money.of(10000);
            Money b = Money.of(5000);

            assertThat(a.isGreaterThan(b)).isTrue();
            assertThat(b.isGreaterThan(a)).isFalse();
        }

        @Test
        @DisplayName("작은 금액을 비교할 수 있다")
        void shouldCompareLessThan() {
            Money a = Money.of(5000);
            Money b = Money.of(10000);

            assertThat(a.isLessThan(b)).isTrue();
            assertThat(b.isLessThan(a)).isFalse();
        }

        @Test
        @DisplayName("같은 금액은 greaterThan, lessThan 모두 false")
        void sameAmountShouldBeNeitherGreaterNorLess() {
            Money a = Money.of(10000);
            Money b = Money.of(10000);

            assertThat(a.isGreaterThan(b)).isFalse();
            assertThat(a.isLessThan(b)).isFalse();
        }
    }

    @Nested
    @DisplayName("동등성 검증")
    class Equality {

        @Test
        @DisplayName("같은 값의 Money는 동등하다")
        void sameValueShouldBeEqual() {
            assertThat(Money.of(10000)).isEqualTo(Money.of(10000));
        }

        @Test
        @DisplayName("다른 값의 Money는 동등하지 않다")
        void differentValueShouldNotBeEqual() {
            assertThat(Money.of(10000)).isNotEqualTo(Money.of(20000));
        }
    }

    @Nested
    @DisplayName("불변성 검증")
    class Immutability {

        @Test
        @DisplayName("add 연산 후 원본은 변경되지 않는다")
        void addShouldNotMutateOriginal() {
            Money original = Money.of(10000);
            original.add(Money.of(5000));

            assertThat(original.amount()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        }

        @Test
        @DisplayName("multiply 연산 후 원본은 변경되지 않는다")
        void multiplyShouldNotMutateOriginal() {
            Money original = Money.of(10000);
            original.multiply(3);

            assertThat(original.amount()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        }
    }
}
