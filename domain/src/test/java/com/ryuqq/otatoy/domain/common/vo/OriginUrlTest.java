package com.ryuqq.otatoy.domain.common.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OriginUrlTest {

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("유효한 URL로 생성할 수 있다")
        void shouldCreateWithValidUrl() {
            OriginUrl url = OriginUrl.of("https://example.com/image.jpg");
            assertThat(url.value()).isEqualTo("https://example.com/image.jpg");
        }

        @Test
        @DisplayName("null이면 예외가 발생한다")
        void shouldThrowWhenNull() {
            assertThatThrownBy(() -> OriginUrl.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("원본 URL은 필수");
        }

        @Test
        @DisplayName("빈 문자열이면 예외가 발생한다")
        void shouldThrowWhenBlank() {
            assertThatThrownBy(() -> OriginUrl.of("  "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("원본 URL은 필수");
        }

        @Test
        @DisplayName("500자 이하면 생성 성공한다")
        void shouldCreateWith500Chars() {
            String url = "https://example.com/" + "a".repeat(480);
            assertThat(url.length()).isEqualTo(500);

            OriginUrl result = OriginUrl.of(url);
            assertThat(result.value()).isEqualTo(url);
        }

        @Test
        @DisplayName("501자 이상이면 예외가 발생한다")
        void shouldThrowWhenOver500Chars() {
            String url = "https://example.com/" + "a".repeat(481);
            assertThat(url.length()).isEqualTo(501);

            assertThatThrownBy(() -> OriginUrl.of(url))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("500자 이하");
        }
    }

    @Nested
    @DisplayName("동등성 검증")
    class Equality {

        @Test
        @DisplayName("같은 값의 OriginUrl은 동등하다")
        void sameValueShouldBeEqual() {
            assertThat(OriginUrl.of("https://example.com/a.jpg"))
                    .isEqualTo(OriginUrl.of("https://example.com/a.jpg"));
        }
    }
}
