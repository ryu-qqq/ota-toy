package com.ryuqq.otatoy.domain.propertytype;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PropertyType VO 검증")
class PropertyTypeVoTest {

    @Nested
    @DisplayName("PropertyTypeId")
    class PropertyTypeIdTest {

        @Test
        @DisplayName("of()로 생성할 수 있다")
        void shouldCreateWithOf() {
            PropertyTypeId id = PropertyTypeId.of(1L);
            assertThat(id.value()).isEqualTo(1L);
        }

        @Test
        @DisplayName("null 값은 isNew() true를 반환한다")
        void nullValueShouldBeNew() {
            PropertyTypeId id = PropertyTypeId.of(null);
            assertThat(id.isNew()).isTrue();
        }

        @Test
        @DisplayName("값이 있으면 isNew() false를 반환한다")
        void nonNullValueShouldNotBeNew() {
            PropertyTypeId id = PropertyTypeId.of(1L);
            assertThat(id.isNew()).isFalse();
        }

        @Test
        @DisplayName("forNew()는 null ID를 가진 PropertyTypeId를 반환한다")
        void forNewShouldReturnNullId() {
            PropertyTypeId id = PropertyTypeId.forNew();
            assertThat(id.isNew()).isTrue();
        }

        @Test
        @DisplayName("같은 값의 PropertyTypeId는 동등하다")
        void sameValueShouldBeEqual() {
            assertThat(PropertyTypeId.of(1L)).isEqualTo(PropertyTypeId.of(1L));
        }
    }

    @Nested
    @DisplayName("PropertyTypeAttributeId")
    class PropertyTypeAttributeIdTest {

        @Test
        @DisplayName("of()로 생성할 수 있다")
        void shouldCreateWithOf() {
            PropertyTypeAttributeId id = PropertyTypeAttributeId.of(1L);
            assertThat(id.value()).isEqualTo(1L);
        }

        @Test
        @DisplayName("null 값은 isNew() true를 반환한다")
        void nullValueShouldBeNew() {
            PropertyTypeAttributeId id = PropertyTypeAttributeId.of(null);
            assertThat(id.isNew()).isTrue();
        }

        @Test
        @DisplayName("forNew()는 null ID를 반환한다")
        void forNewShouldReturnNullId() {
            PropertyTypeAttributeId id = PropertyTypeAttributeId.forNew();
            assertThat(id.isNew()).isTrue();
        }
    }

    @Nested
    @DisplayName("PropertyTypeCode")
    class PropertyTypeCodeTest {

        @Test
        @DisplayName("정상 값으로 생성할 수 있다")
        void shouldCreateWithValidValue() {
            PropertyTypeCode code = PropertyTypeCode.of("HOTEL");
            assertThat(code.value()).isEqualTo("HOTEL");
        }

        @Test
        @DisplayName("null이면 예외가 발생한다")
        void shouldThrowWhenNull() {
            assertThatThrownBy(() -> PropertyTypeCode.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("숙소 유형 코드는 필수");
        }

        @Test
        @DisplayName("빈 문자열이면 예외가 발생한다")
        void shouldThrowWhenBlank() {
            assertThatThrownBy(() -> PropertyTypeCode.of("  "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("숙소 유형 코드는 필수");
        }

        @Test
        @DisplayName("50자 이하면 생성 성공")
        void shouldSucceedWhenExactly50Chars() {
            String value = "A".repeat(50);
            PropertyTypeCode code = PropertyTypeCode.of(value);
            assertThat(code.value()).hasSize(50);
        }

        @Test
        @DisplayName("51자면 생성 실패")
        void shouldFailWhenExceeds50Chars() {
            String value = "A".repeat(51);
            assertThatThrownBy(() -> PropertyTypeCode.of(value))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("50자 이하");
        }

        @Test
        @DisplayName("같은 값의 PropertyTypeCode는 동등하다")
        void sameValueShouldBeEqual() {
            assertThat(PropertyTypeCode.of("HOTEL")).isEqualTo(PropertyTypeCode.of("HOTEL"));
        }
    }

    @Nested
    @DisplayName("PropertyTypeName")
    class PropertyTypeNameTest {

        @Test
        @DisplayName("정상 값으로 생성할 수 있다")
        void shouldCreateWithValidValue() {
            PropertyTypeName name = PropertyTypeName.of("호텔");
            assertThat(name.value()).isEqualTo("호텔");
        }

        @Test
        @DisplayName("null이면 예외가 발생한다")
        void shouldThrowWhenNull() {
            assertThatThrownBy(() -> PropertyTypeName.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("숙소 유형명은 필수");
        }

        @Test
        @DisplayName("빈 문자열이면 예외가 발생한다")
        void shouldThrowWhenBlank() {
            assertThatThrownBy(() -> PropertyTypeName.of("  "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("숙소 유형명은 필수");
        }

        @Test
        @DisplayName("200자 이하면 생성 성공")
        void shouldSucceedWhenExactly200Chars() {
            String value = "가".repeat(200);
            PropertyTypeName name = PropertyTypeName.of(value);
            assertThat(name.value()).hasSize(200);
        }

        @Test
        @DisplayName("201자면 생성 실패")
        void shouldFailWhenExceeds200Chars() {
            String value = "가".repeat(201);
            assertThatThrownBy(() -> PropertyTypeName.of(value))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("200자 이하");
        }

        @Test
        @DisplayName("같은 값의 PropertyTypeName은 동등하다")
        void sameValueShouldBeEqual() {
            assertThat(PropertyTypeName.of("호텔")).isEqualTo(PropertyTypeName.of("호텔"));
        }
    }

    @Nested
    @DisplayName("PropertyTypeDescription")
    class PropertyTypeDescriptionTest {

        @Test
        @DisplayName("정상 값으로 생성할 수 있다")
        void shouldCreateWithValidValue() {
            PropertyTypeDescription desc = PropertyTypeDescription.of("일반 호텔");
            assertThat(desc.value()).isEqualTo("일반 호텔");
        }

        @Test
        @DisplayName("null을 허용한다")
        void shouldAllowNull() {
            PropertyTypeDescription desc = PropertyTypeDescription.of(null);
            assertThat(desc.value()).isNull();
        }

        @Test
        @DisplayName("2000자 이하면 생성 성공")
        void shouldSucceedWhenExactly2000Chars() {
            String value = "가".repeat(2000);
            PropertyTypeDescription desc = PropertyTypeDescription.of(value);
            assertThat(desc.value()).hasSize(2000);
        }

        @Test
        @DisplayName("2001자면 생성 실패")
        void shouldFailWhenExceeds2000Chars() {
            String value = "가".repeat(2001);
            assertThatThrownBy(() -> PropertyTypeDescription.of(value))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("2000자 이하");
        }
    }
}
