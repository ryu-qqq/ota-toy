package com.ryuqq.otatoy.domain.accommodation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PropertyNameTest {

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("정상적인 숙소명 생성")
        void shouldCreatePropertyNameSuccessfully() {
            // when
            PropertyName name = PropertyName.of("테스트 숙소");

            // then
            assertThat(name.value()).isEqualTo("테스트 숙소");
        }

        @Test
        @DisplayName("숙소명이 빈 값이면 생성 실패")
        void shouldFailWhenPropertyNameIsBlank() {
            assertThatThrownBy(() -> PropertyName.of(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("숙소명은 필수");
        }

        @Test
        @DisplayName("숙소명이 null이면 생성 실패")
        void shouldFailWhenPropertyNameIsNull() {
            assertThatThrownBy(() -> PropertyName.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("숙소명은 필수");
        }

        @Test
        @DisplayName("숙소명이 100자이면 생성 성공")
        void shouldSucceedWhenPropertyNameIsExactly100Chars() {
            // given
            String name = "가".repeat(100);

            // when
            PropertyName propertyName = PropertyName.of(name);

            // then
            assertThat(propertyName.value()).hasSize(100);
        }

        @Test
        @DisplayName("숙소명이 101자이면 생성 실패")
        void shouldFailWhenPropertyNameExceeds100Chars() {
            // given
            String name = "가".repeat(101);

            // when & then
            assertThatThrownBy(() -> PropertyName.of(name))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("100자 이하");
        }

        @Test
        @DisplayName("숙소명이 공백만으로 구성되면 생성 실패")
        void shouldFailWhenPropertyNameIsWhitespaceOnly() {
            assertThatThrownBy(() -> PropertyName.of("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("숙소명은 필수");
        }
    }
}
