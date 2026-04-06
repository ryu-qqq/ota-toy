package com.ryuqq.otatoy.domain.common.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhoneNumberTest {

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("유효한 전화번호로 생성할 수 있다")
        void shouldCreateWithValidNumber() {
            PhoneNumber phone = PhoneNumber.of("010-1234-5678");
            assertThat(phone.value()).isEqualTo("010-1234-5678");
        }

        @Test
        @DisplayName("하이픈 없는 숫자만으로 생성할 수 있다")
        void shouldCreateWithDigitsOnly() {
            PhoneNumber phone = PhoneNumber.of("01012345678");
            assertThat(phone.value()).isEqualTo("01012345678");
        }

        @Test
        @DisplayName("null이면 생성 성공한다 (nullable)")
        void shouldCreateWithNull() {
            PhoneNumber phone = PhoneNumber.of(null);
            assertThat(phone.value()).isNull();
        }

        @Test
        @DisplayName("빈 문자열이면 예외가 발생한다")
        void shouldThrowWhenBlank() {
            assertThatThrownBy(() -> PhoneNumber.of("  "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("비어있을 수 없습니다");
        }

        @Test
        @DisplayName("30자 초과이면 예외가 발생한다")
        void shouldThrowWhenOver30Chars() {
            String longNumber = "1".repeat(31);
            assertThatThrownBy(() -> PhoneNumber.of(longNumber))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("30자 이하");
        }

        @Test
        @DisplayName("문자가 포함되면 예외가 발생한다")
        void shouldThrowWhenContainsLetters() {
            assertThatThrownBy(() -> PhoneNumber.of("010-abcd-5678"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("숫자와 하이픈만");
        }

        @Test
        @DisplayName("특수문자가 포함되면 예외가 발생한다")
        void shouldThrowWhenContainsSpecialChars() {
            assertThatThrownBy(() -> PhoneNumber.of("010+1234+5678"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("숫자와 하이픈만");
        }
    }

    @Nested
    @DisplayName("동등성 검증")
    class Equality {

        @Test
        @DisplayName("같은 값의 PhoneNumber는 동등하다")
        void sameValueShouldBeEqual() {
            assertThat(PhoneNumber.of("010-1234-5678")).isEqualTo(PhoneNumber.of("010-1234-5678"));
        }

        @Test
        @DisplayName("null PhoneNumber끼리 동등하다")
        void nullValuesShouldBeEqual() {
            assertThat(PhoneNumber.of(null)).isEqualTo(PhoneNumber.of(null));
        }
    }
}
