package com.ryuqq.otatoy.domain.accommodation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AmenityName VO 검증")
class AmenityNameTest {

    @Nested
    @DisplayName("생성 검증")
    class CreationTest {

        @Test
        @DisplayName("정상 값으로 생성할 수 있다")
        void shouldCreateWithValidValue() {
            AmenityName name = AmenityName.of("와이파이");
            assertThat(name.value()).isEqualTo("와이파이");
        }

        @Test
        @DisplayName("null이면 예외가 발생한다")
        void shouldThrowWhenNull() {
            assertThatThrownBy(() -> AmenityName.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("편의시설명은 필수");
        }

        @Test
        @DisplayName("빈 문자열이면 예외가 발생한다")
        void shouldThrowWhenBlank() {
            assertThatThrownBy(() -> AmenityName.of("  "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("편의시설명은 필수");
        }

        @Test
        @DisplayName("200자 이하면 생성 성공")
        void shouldSucceedWhenExactly200Chars() {
            String value = "가".repeat(200);
            AmenityName name = AmenityName.of(value);
            assertThat(name.value()).hasSize(200);
        }

        @Test
        @DisplayName("201자면 생성 실패")
        void shouldFailWhenExceeds200Chars() {
            String value = "가".repeat(201);
            assertThatThrownBy(() -> AmenityName.of(value))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("200자 이하");
        }
    }

    @Nested
    @DisplayName("동등성 검증")
    class EqualityTest {

        @Test
        @DisplayName("같은 값의 AmenityName은 동등하다")
        void sameValueShouldBeEqual() {
            assertThat(AmenityName.of("와이파이")).isEqualTo(AmenityName.of("와이파이"));
        }
    }
}
