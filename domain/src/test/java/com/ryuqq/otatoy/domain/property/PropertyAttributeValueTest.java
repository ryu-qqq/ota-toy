package com.ryuqq.otatoy.domain.property;

import com.ryuqq.otatoy.domain.common.vo.DeletionStatus;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeAttributeId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PropertyAttributeValue 생성/상태 전이/도메인 로직 테스트.
 */
class PropertyAttributeValueTest {

    private static final PropertyId PROPERTY_ID = PropertyId.of(1L);
    private static final PropertyTypeAttributeId ATTR_ID = PropertyTypeAttributeId.of(10L);
    private static final Instant NOW = Instant.parse("2026-04-04T00:00:00Z");

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("정상 생성")
        void shouldCreateSuccessfully() {
            PropertyAttributeValue av = PropertyAttributeValue.forNew(PROPERTY_ID, ATTR_ID, "5성급", NOW);

            assertThat(av.id().isNew()).isTrue();
            assertThat(av.propertyId()).isEqualTo(PROPERTY_ID);
            assertThat(av.propertyTypeAttributeId()).isEqualTo(ATTR_ID);
            assertThat(av.value()).isEqualTo("5성급");
            assertThat(av.isDeleted()).isFalse();
            assertThat(av.createdAt()).isEqualTo(NOW);
            assertThat(av.updatedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("propertyId가 null이면 생성 실패")
        void shouldFailWhenPropertyIdIsNull() {
            assertThatThrownBy(() -> PropertyAttributeValue.forNew(null, ATTR_ID, "값", NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("숙소 ID는 필수");
        }

        @Test
        @DisplayName("propertyTypeAttributeId가 null이면 생성 실패")
        void shouldFailWhenAttributeIdIsNull() {
            assertThatThrownBy(() -> PropertyAttributeValue.forNew(PROPERTY_ID, null, "값", NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("숙소 유형 속성 ID는 필수");
        }

        @Test
        @DisplayName("propertyTypeAttributeId의 value가 null이면 생성 실패")
        void shouldFailWhenAttributeIdValueIsNull() {
            assertThatThrownBy(() -> PropertyAttributeValue.forNew(
                    PROPERTY_ID, PropertyTypeAttributeId.of(null), "값", NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("숙소 유형 속성 ID는 필수");
        }
    }

    @Nested
    @DisplayName("상태 전이 -- soft delete")
    class SoftDelete {

        @Test
        @DisplayName("delete() 호출 시 isDeleted() true, updatedAt 갱신")
        void shouldDeleteSuccessfully() {
            PropertyAttributeValue av = PropertyAttributeValue.forNew(PROPERTY_ID, ATTR_ID, "값", NOW);
            Instant deleteTime = NOW.plusSeconds(60);

            av.delete(deleteTime);

            assertThat(av.isDeleted()).isTrue();
            assertThat(av.updatedAt()).isEqualTo(deleteTime);
            assertThat(av.deletionStatus().deletedAt()).isEqualTo(deleteTime);
        }

        @Test
        @DisplayName("이미 삭제된 상태에서 delete()는 무시된다 (멱등)")
        void shouldIgnoreDoubleDelete() {
            PropertyAttributeValue av = PropertyAttributeValue.forNew(PROPERTY_ID, ATTR_ID, "값", NOW);
            Instant firstDelete = NOW.plusSeconds(60);
            Instant secondDelete = NOW.plusSeconds(120);

            av.delete(firstDelete);
            av.delete(secondDelete);

            assertThat(av.isDeleted()).isTrue();
            // 첫 번째 삭제 시점이 유지되어야 한다
            assertThat(av.deletionStatus().deletedAt()).isEqualTo(firstDelete);
            assertThat(av.updatedAt()).isEqualTo(firstDelete);
        }
    }

    @Nested
    @DisplayName("도메인 로직")
    class DomainLogic {

        @Test
        @DisplayName("attributeKey()는 propertyTypeAttributeId의 value를 반환한다")
        void shouldReturnAttributeKey() {
            PropertyAttributeValue av = PropertyAttributeValue.forNew(PROPERTY_ID, ATTR_ID, "값", NOW);
            assertThat(av.attributeKey()).isEqualTo(10L);
        }
    }

    @Nested
    @DisplayName("reconstitute 복원 검증")
    class Reconstitute {

        @Test
        @DisplayName("모든 필드가 올바르게 복원된다")
        void shouldReconstituteWithAllFields() {
            PropertyAttributeValue av = PropertyAttributeValue.reconstitute(
                    PropertyAttributeValueId.of(99L), PROPERTY_ID, ATTR_ID,
                    "5성급", NOW, NOW, DeletionStatus.active()
            );

            assertThat(av.id()).isEqualTo(PropertyAttributeValueId.of(99L));
            assertThat(av.id().isNew()).isFalse();
            assertThat(av.value()).isEqualTo("5성급");
            assertThat(av.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("삭제된 상태로 복원 가능")
        void shouldReconstituteAsDeleted() {
            PropertyAttributeValue av = PropertyAttributeValue.reconstitute(
                    PropertyAttributeValueId.of(99L), PROPERTY_ID, ATTR_ID,
                    "5성급", NOW, NOW, DeletionStatus.deleted(NOW)
            );

            assertThat(av.isDeleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("equals/hashCode 검증")
    class Equality {

        @Test
        @DisplayName("동일 ID면 equals true")
        void shouldBeEqualWithSameId() {
            PropertyAttributeValue a = PropertyAttributeValue.reconstitute(
                    PropertyAttributeValueId.of(1L), PROPERTY_ID, ATTR_ID,
                    "값A", NOW, NOW, DeletionStatus.active()
            );
            PropertyAttributeValue b = PropertyAttributeValue.reconstitute(
                    PropertyAttributeValueId.of(1L), PropertyId.of(2L),
                    PropertyTypeAttributeId.of(20L), "값B", NOW, NOW, DeletionStatus.active()
            );

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("다른 ID면 equals false")
        void shouldNotBeEqualWithDifferentId() {
            PropertyAttributeValue a = PropertyAttributeValue.reconstitute(
                    PropertyAttributeValueId.of(1L), PROPERTY_ID, ATTR_ID,
                    "값", NOW, NOW, DeletionStatus.active()
            );
            PropertyAttributeValue b = PropertyAttributeValue.reconstitute(
                    PropertyAttributeValueId.of(2L), PROPERTY_ID, ATTR_ID,
                    "값", NOW, NOW, DeletionStatus.active()
            );

            assertThat(a).isNotEqualTo(b);
        }
    }
}
