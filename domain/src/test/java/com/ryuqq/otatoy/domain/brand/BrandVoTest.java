package com.ryuqq.otatoy.domain.brand;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Brand VO 검증")
class BrandVoTest {

    @Nested
    @DisplayName("BrandId")
    class BrandIdTest {

        @Test
        @DisplayName("of()로 생성할 수 있다")
        void shouldCreateWithOf() {
            BrandId id = BrandId.of(1L);
            assertThat(id.value()).isEqualTo(1L);
        }

        @Test
        @DisplayName("null 값은 isNew() true를 반환한다")
        void nullValueShouldBeNew() {
            BrandId id = BrandId.of(null);
            assertThat(id.isNew()).isTrue();
        }

        @Test
        @DisplayName("값이 있으면 isNew() false를 반환한다")
        void nonNullValueShouldNotBeNew() {
            BrandId id = BrandId.of(1L);
            assertThat(id.isNew()).isFalse();
        }

        @Test
        @DisplayName("forNew()는 null ID를 가진 BrandId를 반환한다")
        void forNewShouldReturnNullId() {
            BrandId id = BrandId.forNew();
            assertThat(id.isNew()).isTrue();
            assertThat(id.value()).isNull();
        }

        @Test
        @DisplayName("같은 값의 BrandId는 동등하다")
        void sameValueShouldBeEqual() {
            assertThat(BrandId.of(1L)).isEqualTo(BrandId.of(1L));
        }
    }

    @Nested
    @DisplayName("BrandName")
    class BrandNameTest {

        @Test
        @DisplayName("정상 값으로 생성할 수 있다")
        void shouldCreateWithValidValue() {
            BrandName name = BrandName.of("TestBrand");
            assertThat(name.value()).isEqualTo("TestBrand");
        }

        @Test
        @DisplayName("null이면 예외가 발생한다")
        void shouldThrowWhenNull() {
            assertThatThrownBy(() -> BrandName.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("브랜드명은 필수");
        }

        @Test
        @DisplayName("빈 문자열이면 예외가 발생한다")
        void shouldThrowWhenBlank() {
            assertThatThrownBy(() -> BrandName.of("  "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("브랜드명은 필수");
        }

        @Test
        @DisplayName("100자 이하면 생성 성공")
        void shouldSucceedWhenExactly100Chars() {
            String name = "가".repeat(100);
            BrandName brandName = BrandName.of(name);
            assertThat(brandName.value()).hasSize(100);
        }

        @Test
        @DisplayName("101자면 생성 실패")
        void shouldFailWhenExceeds100Chars() {
            String name = "가".repeat(101);
            assertThatThrownBy(() -> BrandName.of(name))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("100자 이하");
        }

        @Test
        @DisplayName("같은 값의 BrandName은 동등하다")
        void sameValueShouldBeEqual() {
            assertThat(BrandName.of("테스트")).isEqualTo(BrandName.of("테스트"));
        }
    }

    @Nested
    @DisplayName("BrandNameKr")
    class BrandNameKrTest {

        @Test
        @DisplayName("정상 값으로 생성할 수 있다")
        void shouldCreateWithValidValue() {
            BrandNameKr nameKr = BrandNameKr.of("테스트브랜드");
            assertThat(nameKr.value()).isEqualTo("테스트브랜드");
        }

        @Test
        @DisplayName("null을 허용한다")
        void shouldAllowNull() {
            BrandNameKr nameKr = BrandNameKr.of(null);
            assertThat(nameKr.value()).isNull();
        }

        @Test
        @DisplayName("200자 이하면 생성 성공")
        void shouldSucceedWhenExactly200Chars() {
            String value = "가".repeat(200);
            BrandNameKr nameKr = BrandNameKr.of(value);
            assertThat(nameKr.value()).hasSize(200);
        }

        @Test
        @DisplayName("201자면 생성 실패")
        void shouldFailWhenExceeds200Chars() {
            String value = "가".repeat(201);
            assertThatThrownBy(() -> BrandNameKr.of(value))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("200자 이하");
        }
    }

    @Nested
    @DisplayName("LogoUrl")
    class LogoUrlTest {

        @Test
        @DisplayName("정상 URL로 생성할 수 있다")
        void shouldCreateWithValidValue() {
            LogoUrl url = LogoUrl.of("https://example.com/logo.png");
            assertThat(url.value()).isEqualTo("https://example.com/logo.png");
        }

        @Test
        @DisplayName("null을 허용한다")
        void shouldAllowNull() {
            LogoUrl url = LogoUrl.of(null);
            assertThat(url.value()).isNull();
        }

        @Test
        @DisplayName("500자 이하면 생성 성공")
        void shouldSucceedWhenExactly500Chars() {
            String value = "a".repeat(500);
            LogoUrl url = LogoUrl.of(value);
            assertThat(url.value()).hasSize(500);
        }

        @Test
        @DisplayName("501자면 생성 실패")
        void shouldFailWhenExceeds500Chars() {
            String value = "a".repeat(501);
            assertThatThrownBy(() -> LogoUrl.of(value))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("500자 이하");
        }
    }
}
