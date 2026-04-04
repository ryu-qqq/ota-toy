package com.ryuqq.otatoy.domain.property;

import com.ryuqq.otatoy.domain.accommodation.AmenityName;
import com.ryuqq.otatoy.domain.accommodation.AmenityType;

import com.ryuqq.otatoy.domain.common.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PropertyAmenityTest {

    private static final PropertyId PROPERTY_ID = PropertyId.of(1L);
    private static final AmenityName POOL_NAME = AmenityName.of("수영장");
    private static final AmenityName WIFI_NAME = AmenityName.of("와이파이");

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("PropertyAmenity 정상 생성")
        void shouldCreatePropertyAmenitySuccessfully() {
            // when
            PropertyAmenity amenity = PropertyAmenity.forNew(PROPERTY_ID, AmenityType.POOL, POOL_NAME, Money.of(5000), 1);

            // then
            assertThat(amenity).isNotNull();
            assertThat(amenity.id()).isNotNull();
            assertThat(amenity.id().isNew()).isTrue();
            assertThat(amenity.propertyId()).isEqualTo(PROPERTY_ID);
            assertThat(amenity.amenityType()).isEqualTo(AmenityType.POOL);
            assertThat(amenity.name()).isEqualTo(POOL_NAME);
        }

        @Test
        @DisplayName("amenityType이 null이면 생성 실패")
        void shouldFailWhenAmenityTypeIsNull() {
            assertThatThrownBy(() -> PropertyAmenity.forNew(PROPERTY_ID, null, POOL_NAME, Money.of(0), 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("편의시설 유형은 필수");
        }

        @Test
        @DisplayName("편의시설명이 null이면 생성 실패")
        void shouldFailWhenNameIsNull() {
            assertThatThrownBy(() -> AmenityName.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("편의시설명은 필수");
        }

        @Test
        @DisplayName("편의시설명이 빈 값이면 생성 실패")
        void shouldFailWhenNameIsBlank() {
            assertThatThrownBy(() -> AmenityName.of(" "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("편의시설명은 필수");
        }
    }

    @Nested
    @DisplayName("도메인 로직")
    class DomainLogic {

        @Test
        @DisplayName("isFree() — additionalPrice가 null이면 무료")
        void shouldBeFreeWhenAdditionalPriceIsNull() {
            // given
            PropertyAmenity amenity = PropertyAmenity.forNew(PROPERTY_ID, AmenityType.WIFI, WIFI_NAME, null, 1);

            // when & then
            assertThat(amenity.isFree()).isTrue();
        }

        @Test
        @DisplayName("isFree() — additionalPrice가 0이면 무료")
        void shouldBeFreeWhenAdditionalPriceIsZero() {
            // given
            PropertyAmenity amenity = PropertyAmenity.forNew(PROPERTY_ID, AmenityType.WIFI, WIFI_NAME, Money.of(0), 1);

            // when & then
            assertThat(amenity.isFree()).isTrue();
        }

        @Test
        @DisplayName("isFree() — additionalPrice가 1000이면 유료")
        void shouldNotBeFreeWhenAdditionalPriceIsPositive() {
            // given
            PropertyAmenity amenity = PropertyAmenity.forNew(PROPERTY_ID, AmenityType.POOL, POOL_NAME, Money.of(1000), 1);

            // when & then
            assertThat(amenity.isFree()).isFalse();
        }
    }
}
