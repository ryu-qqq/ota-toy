package com.ryuqq.otatoy.domain.propertytype;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PropertyTypeAttribute 엔티티 검증")
class PropertyTypeAttributeTest {

    private static final Instant NOW = PropertyTypeFixture.DEFAULT_NOW;

    @Nested
    @DisplayName("T-1: forNew() 생성 검증")
    class ForNewTest {

        @Test
        @DisplayName("유효한 값으로 필수 속성을 생성할 수 있다")
        void shouldCreateRequiredAttribute() {
            PropertyTypeAttribute attr = PropertyTypeFixture.requiredAttribute();

            assertThat(attr.id().isNew()).isTrue();
            assertThat(attr.propertyTypeId()).isEqualTo(PropertyTypeId.of(1L));
            assertThat(attr.attributeKey()).isEqualTo("star_rating");
            assertThat(attr.attributeName()).isEqualTo("성급");
            assertThat(attr.valueType()).isEqualTo("INTEGER");
            assertThat(attr.required()).isTrue();
            assertThat(attr.sortOrder()).isEqualTo(1);
            assertThat(attr.createdAt()).isEqualTo(NOW);
            assertThat(attr.updatedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("유효한 값으로 선택 속성을 생성할 수 있다")
        void shouldCreateOptionalAttribute() {
            PropertyTypeAttribute attr = PropertyTypeFixture.optionalAttribute();

            assertThat(attr.required()).isFalse();
            assertThat(attr.sortOrder()).isEqualTo(2);
        }

        @Test
        @DisplayName("attributeKey가 null이면 예외가 발생한다")
        void shouldThrowWhenAttributeKeyIsNull() {
            assertThatThrownBy(() -> PropertyTypeAttribute.forNew(
                    PropertyTypeId.of(1L), null, "성급", "INTEGER", true, 1, NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("속성 키는 필수");
        }

        @Test
        @DisplayName("attributeKey가 빈 문자열이면 예외가 발생한다")
        void shouldThrowWhenAttributeKeyIsBlank() {
            assertThatThrownBy(() -> PropertyTypeAttribute.forNew(
                    PropertyTypeId.of(1L), "  ", "성급", "INTEGER", true, 1, NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("속성 키는 필수");
        }

        @Test
        @DisplayName("attributeName이 null이면 예외가 발생한다")
        void shouldThrowWhenAttributeNameIsNull() {
            assertThatThrownBy(() -> PropertyTypeAttribute.forNew(
                    PropertyTypeId.of(1L), "star_rating", null, "INTEGER", true, 1, NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("속성명은 필수");
        }

        @Test
        @DisplayName("attributeName이 빈 문자열이면 예외가 발생한다")
        void shouldThrowWhenAttributeNameIsBlank() {
            assertThatThrownBy(() -> PropertyTypeAttribute.forNew(
                    PropertyTypeId.of(1L), "star_rating", "  ", "INTEGER", true, 1, NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("속성명은 필수");
        }

        @Test
        @DisplayName("valueType이 null이면 예외가 발생한다")
        void shouldThrowWhenValueTypeIsNull() {
            assertThatThrownBy(() -> PropertyTypeAttribute.forNew(
                    PropertyTypeId.of(1L), "star_rating", "성급", null, true, 1, NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("값 유형은 필수");
        }

        @Test
        @DisplayName("valueType이 빈 문자열이면 예외가 발생한다")
        void shouldThrowWhenValueTypeIsBlank() {
            assertThatThrownBy(() -> PropertyTypeAttribute.forNew(
                    PropertyTypeId.of(1L), "star_rating", "성급", "  ", true, 1, NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("값 유형은 필수");
        }
    }

    @Nested
    @DisplayName("T-2: reconstitute() 복원 검증")
    class ReconstituteTest {

        @Test
        @DisplayName("DB 복원 시 모든 필드가 그대로 복원된다")
        void shouldReconstituteAllFields() {
            PropertyTypeAttribute attr = PropertyTypeFixture.reconstitutedAttribute();

            assertThat(attr.id()).isEqualTo(PropertyTypeAttributeId.of(1L));
            assertThat(attr.propertyTypeId()).isEqualTo(PropertyTypeId.of(1L));
            assertThat(attr.attributeKey()).isEqualTo("star_rating");
            assertThat(attr.attributeName()).isEqualTo("성급");
            assertThat(attr.valueType()).isEqualTo("INTEGER");
            assertThat(attr.required()).isTrue();
            assertThat(attr.sortOrder()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("T-3: equals/hashCode")
    class EqualsHashCodeTest {

        @Test
        @DisplayName("같은 ID를 가진 PropertyTypeAttribute는 동등하다")
        void sameIdShouldBeEqual() {
            PropertyTypeAttribute a1 = PropertyTypeFixture.reconstitutedAttribute();
            PropertyTypeAttribute a2 = PropertyTypeAttribute.reconstitute(
                    PropertyTypeAttributeId.of(1L), PropertyTypeId.of(2L),
                    "other_key", "다른속성", "STRING",
                    false, 99, NOW, NOW
            );

            assertThat(a1).isEqualTo(a2);
            assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
        }

        @Test
        @DisplayName("다른 ID를 가진 PropertyTypeAttribute는 동등하지 않다")
        void differentIdShouldNotBeEqual() {
            PropertyTypeAttribute a1 = PropertyTypeAttribute.reconstitute(
                    PropertyTypeAttributeId.of(1L), PropertyTypeId.of(1L),
                    "key1", "속성1", "STRING", true, 1, NOW, NOW
            );
            PropertyTypeAttribute a2 = PropertyTypeAttribute.reconstitute(
                    PropertyTypeAttributeId.of(2L), PropertyTypeId.of(1L),
                    "key2", "속성2", "STRING", true, 2, NOW, NOW
            );

            assertThat(a1).isNotEqualTo(a2);
        }
    }
}
