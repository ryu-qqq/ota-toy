package com.ryuqq.otatoy.domain.property;

import com.ryuqq.otatoy.domain.accommodation.AccommodationErrorCode;
import com.ryuqq.otatoy.domain.common.ErrorCategory;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeAttributeId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Property BC의 VO, Enum, ErrorCode, Exception 테스트.
 * 기존 테스트(LocationTest, PropertyNameTest, PropertyTest, PropertyPhotoTest, PropertyAmenityTest)에서
 * 커버되지 않는 항목을 보강한다.
 */
class PropertyVoTest {

    // ========== PropertyId ==========

    @Nested
    @DisplayName("PropertyId")
    class PropertyIdTest {

        @Test
        @DisplayName("of()로 생성하면 value가 올바르다")
        void shouldCreateWithValue() {
            PropertyId id = PropertyId.of(42L);
            assertThat(id.value()).isEqualTo(42L);
        }

        @Test
        @DisplayName("null value이면 isNew() true")
        void shouldBeNewWhenNull() {
            PropertyId id = PropertyId.of(null);
            assertThat(id.isNew()).isTrue();
        }

        @Test
        @DisplayName("non-null value이면 isNew() false")
        void shouldNotBeNewWhenNonNull() {
            PropertyId id = PropertyId.of(1L);
            assertThat(id.isNew()).isFalse();
        }

        @Test
        @DisplayName("forNew()로 생성하면 isNew() true")
        void shouldBeNewViaForNew() {
            PropertyId id = PropertyId.forNew();
            assertThat(id.isNew()).isTrue();
            assertThat(id.value()).isNull();
        }

        @Test
        @DisplayName("동일 value이면 equals true (Record)")
        void shouldBeEqualWithSameValue() {
            assertThat(PropertyId.of(1L)).isEqualTo(PropertyId.of(1L));
        }
    }

    // ========== PropertyAmenityId ==========

    @Nested
    @DisplayName("PropertyAmenityId")
    class PropertyAmenityIdTest {

        @Test
        @DisplayName("of()로 생성하면 value가 올바르다")
        void shouldCreateWithValue() {
            PropertyAmenityId id = PropertyAmenityId.of(10L);
            assertThat(id.value()).isEqualTo(10L);
        }

        @Test
        @DisplayName("forNew()로 생성하면 isNew() true")
        void shouldBeNewViaForNew() {
            PropertyAmenityId id = PropertyAmenityId.forNew();
            assertThat(id.isNew()).isTrue();
        }

        @Test
        @DisplayName("non-null value이면 isNew() false")
        void shouldNotBeNewWhenNonNull() {
            assertThat(PropertyAmenityId.of(1L).isNew()).isFalse();
        }
    }

    // ========== PropertyPhotoId ==========

    @Nested
    @DisplayName("PropertyPhotoId")
    class PropertyPhotoIdTest {

        @Test
        @DisplayName("of()로 생성하면 value가 올바르다")
        void shouldCreateWithValue() {
            PropertyPhotoId id = PropertyPhotoId.of(10L);
            assertThat(id.value()).isEqualTo(10L);
        }

        @Test
        @DisplayName("forNew()로 생성하면 isNew() true")
        void shouldBeNewViaForNew() {
            PropertyPhotoId id = PropertyPhotoId.forNew();
            assertThat(id.isNew()).isTrue();
        }

        @Test
        @DisplayName("non-null value이면 isNew() false")
        void shouldNotBeNewWhenNonNull() {
            assertThat(PropertyPhotoId.of(1L).isNew()).isFalse();
        }
    }

    // ========== PropertyAttributeValueId ==========

    @Nested
    @DisplayName("PropertyAttributeValueId")
    class PropertyAttributeValueIdTest {

        @Test
        @DisplayName("of()로 생성하면 value가 올바르다")
        void shouldCreateWithValue() {
            PropertyAttributeValueId id = PropertyAttributeValueId.of(10L);
            assertThat(id.value()).isEqualTo(10L);
        }

        @Test
        @DisplayName("forNew()로 생성하면 isNew() true")
        void shouldBeNewViaForNew() {
            PropertyAttributeValueId id = PropertyAttributeValueId.forNew();
            assertThat(id.isNew()).isTrue();
        }

        @Test
        @DisplayName("non-null value이면 isNew() false")
        void shouldNotBeNewWhenNonNull() {
            assertThat(PropertyAttributeValueId.of(1L).isNew()).isFalse();
        }
    }

    // ========== PropertyDescription ==========

    @Nested
    @DisplayName("PropertyDescription")
    class PropertyDescriptionTest {

        @Test
        @DisplayName("정상 설명 생성")
        void shouldCreateSuccessfully() {
            PropertyDescription desc = PropertyDescription.of("테스트 설명");
            assertThat(desc.value()).isEqualTo("테스트 설명");
        }

        @Test
        @DisplayName("null이면 성공 (nullable)")
        void shouldAllowNull() {
            PropertyDescription desc = PropertyDescription.of(null);
            assertThat(desc.value()).isNull();
        }

        @Test
        @DisplayName("2000자 이하면 성공")
        void shouldSucceedWhenExactly2000Chars() {
            String text = "가".repeat(2000);
            PropertyDescription desc = PropertyDescription.of(text);
            assertThat(desc.value()).hasSize(2000);
        }

        @Test
        @DisplayName("2001자이면 생성 실패")
        void shouldFailWhenExceeds2000Chars() {
            String text = "가".repeat(2001);
            assertThatThrownBy(() -> PropertyDescription.of(text))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("2000자 이하");
        }
    }

    // ========== PromotionText ==========

    @Nested
    @DisplayName("PromotionText")
    class PromotionTextTest {

        @Test
        @DisplayName("정상 홍보 문구 생성")
        void shouldCreateSuccessfully() {
            PromotionText pt = PromotionText.of("특가 이벤트");
            assertThat(pt.value()).isEqualTo("특가 이벤트");
        }

        @Test
        @DisplayName("null이면 성공 (nullable)")
        void shouldAllowNull() {
            PromotionText pt = PromotionText.of(null);
            assertThat(pt.value()).isNull();
        }

        @Test
        @DisplayName("500자 이하면 성공")
        void shouldSucceedWhenExactly500Chars() {
            String text = "가".repeat(500);
            PromotionText pt = PromotionText.of(text);
            assertThat(pt.value()).hasSize(500);
        }

        @Test
        @DisplayName("501자이면 생성 실패")
        void shouldFailWhenExceeds500Chars() {
            String text = "가".repeat(501);
            assertThatThrownBy(() -> PromotionText.of(text))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("500자 이하");
        }
    }

    // ========== PropertyStatus ==========

    @Nested
    @DisplayName("PropertyStatus")
    class PropertyStatusTest {

        @Test
        @DisplayName("ACTIVE의 displayName은 '운영중'이다")
        void activeDisplayName() {
            assertThat(PropertyStatus.ACTIVE.displayName()).isEqualTo("운영중");
        }

        @Test
        @DisplayName("INACTIVE의 displayName은 '비활성'이다")
        void inactiveDisplayName() {
            assertThat(PropertyStatus.INACTIVE.displayName()).isEqualTo("비활성");
        }

        @Test
        @DisplayName("모든 상태가 displayName()을 반환한다")
        void shouldHaveDisplayName() {
            for (PropertyStatus status : PropertyStatus.values()) {
                assertThat(status.displayName()).isNotNull().isNotBlank();
            }
        }
    }

    // ========== PropertySortKey ==========

    @Nested
    @DisplayName("PropertySortKey")
    class PropertySortKeyTest {

        @Test
        @DisplayName("PRICE_LOW의 fieldName은 'priceLow'이다")
        void priceLowFieldName() {
            assertThat(PropertySortKey.PRICE_LOW.fieldName()).isEqualTo("priceLow");
        }

        @Test
        @DisplayName("PRICE_HIGH의 fieldName은 'priceHigh'이다")
        void priceHighFieldName() {
            assertThat(PropertySortKey.PRICE_HIGH.fieldName()).isEqualTo("priceHigh");
        }

        @Test
        @DisplayName("모든 키가 displayName()을 반환한다")
        void shouldHaveDisplayName() {
            for (PropertySortKey key : PropertySortKey.values()) {
                assertThat(key.displayName()).isNotNull().isNotBlank();
            }
        }
    }

    // ========== PropertyNotFoundException ==========

    @Nested
    @DisplayName("PropertyNotFoundException")
    class PropertyNotFoundExceptionTest {

        @Test
        @DisplayName("에러 코드가 PROPERTY_NOT_FOUND이다")
        void shouldHaveCorrectErrorCode() {
            PropertyNotFoundException ex = new PropertyNotFoundException();
            assertThat(ex.getErrorCode()).isEqualTo(AccommodationErrorCode.PROPERTY_NOT_FOUND);
            assertThat(ex.getMessage()).isEqualTo("숙소를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("RuntimeException이다")
        void shouldBeRuntimeException() {
            PropertyNotFoundException ex = new PropertyNotFoundException();
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    // ========== RequiredPropertyAttributeMissingException ==========

    @Nested
    @DisplayName("RequiredPropertyAttributeMissingException")
    class RequiredPropertyAttributeMissingExceptionTest {

        @Test
        @DisplayName("에러 코드가 REQUIRED_ATTRIBUTE_MISSING이다")
        void shouldHaveCorrectErrorCode() {
            Set<PropertyTypeAttributeId> missing = Set.of(
                    PropertyTypeAttributeId.of(1L), PropertyTypeAttributeId.of(2L)
            );
            RequiredPropertyAttributeMissingException ex = new RequiredPropertyAttributeMissingException(missing);
            assertThat(ex.getErrorCode()).isEqualTo(AccommodationErrorCode.REQUIRED_ATTRIBUTE_MISSING);
            assertThat(ex.category()).isEqualTo(ErrorCategory.VALIDATION);
        }

        @Test
        @DisplayName("누락된 속성 ID 목록을 반환한다")
        void shouldReturnMissingAttributeIds() {
            Set<PropertyTypeAttributeId> missing = Set.of(
                    PropertyTypeAttributeId.of(1L), PropertyTypeAttributeId.of(2L)
            );
            RequiredPropertyAttributeMissingException ex = new RequiredPropertyAttributeMissingException(missing);
            assertThat(ex.missingAttributeIds()).hasSize(2);
            assertThat(ex.missingAttributeIds()).containsExactlyInAnyOrder(
                    PropertyTypeAttributeId.of(1L), PropertyTypeAttributeId.of(2L)
            );
        }
    }
}
