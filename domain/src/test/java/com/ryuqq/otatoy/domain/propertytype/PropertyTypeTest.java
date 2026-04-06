package com.ryuqq.otatoy.domain.propertytype;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PropertyType 엔티티 검증")
class PropertyTypeTest {

    private static final Instant NOW = PropertyTypeFixture.DEFAULT_NOW;
    private static final Instant LATER = NOW.plusSeconds(3600);

    @Nested
    @DisplayName("T-1: forNew() 팩토리 메서드")
    class ForNewTest {

        @Test
        @DisplayName("신규 PropertyType는 id가 null이다")
        void shouldHaveNullId() {
            PropertyType propertyType = PropertyTypeFixture.newPropertyType();

            assertThat(propertyType.id()).isNull();
            assertThat(propertyType.code()).isEqualTo(PropertyTypeFixture.DEFAULT_CODE);
            assertThat(propertyType.name()).isEqualTo(PropertyTypeFixture.DEFAULT_NAME);
            assertThat(propertyType.description()).isEqualTo(PropertyTypeFixture.DEFAULT_DESCRIPTION);
            assertThat(propertyType.createdAt()).isEqualTo(NOW);
            assertThat(propertyType.updatedAt()).isEqualTo(NOW);
        }
    }

    @Nested
    @DisplayName("T-2: reconstitute() 팩토리 메서드")
    class ReconstituteTest {

        @Test
        @DisplayName("DB 복원 시 모든 필드가 그대로 복원된다")
        void shouldReconstituteAllFields() {
            PropertyType pt = PropertyTypeFixture.reconstitutedPropertyType();

            assertThat(pt.id()).isEqualTo(PropertyTypeId.of(1L));
            assertThat(pt.code()).isEqualTo(PropertyTypeFixture.DEFAULT_CODE);
            assertThat(pt.name()).isEqualTo(PropertyTypeFixture.DEFAULT_NAME);
            assertThat(pt.description()).isEqualTo(PropertyTypeFixture.DEFAULT_DESCRIPTION);
            assertThat(pt.createdAt()).isEqualTo(NOW);
            assertThat(pt.updatedAt()).isEqualTo(NOW);
        }
    }

    @Nested
    @DisplayName("T-3: updateInfo() 상태 변경")
    class UpdateInfoTest {

        @Test
        @DisplayName("정보 변경 시 name, description, updatedAt이 갱신된다")
        void shouldUpdateNameDescriptionAndTimestamp() {
            PropertyType pt = PropertyTypeFixture.reconstitutedPropertyType();
            PropertyTypeName newName = PropertyTypeName.of("리조트");
            PropertyTypeDescription newDesc = PropertyTypeDescription.of("고급 리조트");

            pt.updateInfo(newName, newDesc, LATER);

            assertThat(pt.name()).isEqualTo(newName);
            assertThat(pt.description()).isEqualTo(newDesc);
            assertThat(pt.updatedAt()).isEqualTo(LATER);
        }

        @Test
        @DisplayName("정보 변경 후에도 code, id, createdAt은 유지된다")
        void shouldPreserveImmutableFields() {
            PropertyType pt = PropertyTypeFixture.reconstitutedPropertyType();

            pt.updateInfo(PropertyTypeName.of("변경"), PropertyTypeDescription.of("변경 설명"), LATER);

            assertThat(pt.id()).isEqualTo(PropertyTypeId.of(1L));
            assertThat(pt.code()).isEqualTo(PropertyTypeFixture.DEFAULT_CODE);
            assertThat(pt.createdAt()).isEqualTo(NOW);
        }
    }

    @Nested
    @DisplayName("T-4: equals/hashCode")
    class EqualsHashCodeTest {

        @Test
        @DisplayName("같은 ID를 가진 PropertyType는 동등하다")
        void sameIdShouldBeEqual() {
            PropertyType p1 = PropertyTypeFixture.propertyTypeWithId(1L);
            PropertyType p2 = PropertyType.reconstitute(
                    PropertyTypeId.of(1L), PropertyTypeCode.of("RESORT"),
                    PropertyTypeName.of("다른이름"), PropertyTypeDescription.of(null),
                    NOW, LATER
            );

            assertThat(p1).isEqualTo(p2);
            assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
        }

        @Test
        @DisplayName("다른 ID를 가진 PropertyType는 동등하지 않다")
        void differentIdShouldNotBeEqual() {
            PropertyType p1 = PropertyTypeFixture.propertyTypeWithId(1L);
            PropertyType p2 = PropertyTypeFixture.propertyTypeWithId(2L);

            assertThat(p1).isNotEqualTo(p2);
        }

        @Test
        @DisplayName("forNew()로 만든 두 객체는 id가 null이므로 equals false")
        void forNewShouldNotBeEqual() {
            PropertyType p1 = PropertyType.forNew(
                    PropertyTypeCode.of("A"), PropertyTypeName.of("A"), PropertyTypeDescription.of(null), NOW
            );
            PropertyType p2 = PropertyType.forNew(
                    PropertyTypeCode.of("B"), PropertyTypeName.of("B"), PropertyTypeDescription.of(null), NOW
            );

            assertThat(p1).isNotEqualTo(p2);
        }
    }
}
