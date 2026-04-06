package com.ryuqq.otatoy.domain.common.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("유효한 이메일로 생성할 수 있다")
        void shouldCreateWithValidEmail() {
            Email email = Email.of("test@example.com");
            assertThat(email.value()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("null이면 예외가 발생한다")
        void shouldThrowWhenNull() {
            assertThatThrownBy(() -> Email.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이메일은 필수");
        }

        @Test
        @DisplayName("빈 문자열이면 예외가 발생한다")
        void shouldThrowWhenBlank() {
            assertThatThrownBy(() -> Email.of("  "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이메일은 필수");
        }

        @Test
        @DisplayName("@가 없으면 예외가 발생한다")
        void shouldThrowWhenNoAtSign() {
            assertThatThrownBy(() -> Email.of("testexample.com"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("유효하지 않은 이메일 형식");
        }

        @Test
        @DisplayName("200자 이하면 생성 성공한다")
        void shouldCreateWith200Chars() {
            String email = "a".repeat(190) + "@test.com";
            assertThat(email.length()).isLessThanOrEqualTo(200);

            Email result = Email.of(email);
            assertThat(result.value()).isEqualTo(email);
        }

        @Test
        @DisplayName("201자 이상이면 예외가 발생한다")
        void shouldThrowWhenOver200Chars() {
            String email = "a".repeat(192) + "@test.com"; // 201자
            assertThat(email.length()).isGreaterThan(200);

            assertThatThrownBy(() -> Email.of(email))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("200자 이하");
        }
    }

    @Nested
    @DisplayName("동등성 검증")
    class Equality {

        @Test
        @DisplayName("같은 값의 Email은 동등하다")
        void sameValueShouldBeEqual() {
            assertThat(Email.of("test@example.com")).isEqualTo(Email.of("test@example.com"));
        }
    }
}
