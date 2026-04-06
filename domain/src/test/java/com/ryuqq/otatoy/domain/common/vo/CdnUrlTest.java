package com.ryuqq.otatoy.domain.common.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CdnUrlTest {

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("유효한 URL로 생성할 수 있다")
        void shouldCreateWithValidUrl() {
            CdnUrl url = CdnUrl.of("https://cdn.example.com/image.jpg");
            assertThat(url.value()).isEqualTo("https://cdn.example.com/image.jpg");
        }

        @Test
        @DisplayName("null이면 생성 성공한다 (nullable)")
        void shouldCreateWithNull() {
            CdnUrl url = CdnUrl.of(null);
            assertThat(url.value()).isNull();
        }

        @Test
        @DisplayName("빈 문자열이면 예외가 발생한다")
        void shouldThrowWhenBlank() {
            assertThatThrownBy(() -> CdnUrl.of("  "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("CDN URL이 비어있을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("동등성 검증")
    class Equality {

        @Test
        @DisplayName("같은 값의 CdnUrl은 동등하다")
        void sameValueShouldBeEqual() {
            assertThat(CdnUrl.of("https://cdn.example.com/a.jpg"))
                    .isEqualTo(CdnUrl.of("https://cdn.example.com/a.jpg"));
        }

        @Test
        @DisplayName("null CdnUrl끼리 동등하다")
        void nullValuesShouldBeEqual() {
            assertThat(CdnUrl.of(null)).isEqualTo(CdnUrl.of(null));
        }
    }
}
