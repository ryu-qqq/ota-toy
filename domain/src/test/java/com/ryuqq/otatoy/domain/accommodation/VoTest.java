package com.ryuqq.otatoy.domain.accommodation;

import com.ryuqq.otatoy.domain.brand.BrandId;
import com.ryuqq.otatoy.domain.brand.BrandName;
import com.ryuqq.otatoy.domain.partner.PartnerId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("VO 검증")
class VoTest {

    @Nested
    @DisplayName("BrandId")
    class BrandIdTest {

        @Test
        @DisplayName("BrandId null 허용 — isNew() = true")
        void shouldAllowNullBrandId() {
            // when
            BrandId brandId = BrandId.of(null);

            // then
            assertThat(brandId.isNew()).isTrue();
            assertThat(brandId.value()).isNull();
        }

        @Test
        @DisplayName("BrandId 값이 있으면 isNew() = false")
        void shouldNotBeNewWhenValueExists() {
            // when
            BrandId brandId = BrandId.of(1L);

            // then
            assertThat(brandId.isNew()).isFalse();
            assertThat(brandId.value()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("PropertyTypeId")
    class PropertyTypeIdTest {

        @Test
        @DisplayName("PropertyTypeId null 허용 — isNew() = true")
        void shouldAllowNullPropertyTypeId() {
            // when
            PropertyTypeId typeId = PropertyTypeId.of(null);

            // then
            assertThat(typeId.isNew()).isTrue();
            assertThat(typeId.value()).isNull();
        }

        @Test
        @DisplayName("PropertyTypeId 값이 있으면 isNew() = false")
        void shouldNotBeNewWhenValueExists() {
            // when
            PropertyTypeId typeId = PropertyTypeId.of(3L);

            // then
            assertThat(typeId.isNew()).isFalse();
        }
    }

    @Nested
    @DisplayName("PartnerId")
    class PartnerIdTest {

        @Test
        @DisplayName("PartnerId null 허용 — isNew()로 새 엔티티 판별")
        void shouldAllowNullPartnerId() {
            PartnerId partnerId = PartnerId.of(null);
            assertThat(partnerId.isNew()).isTrue();
        }

        @Test
        @DisplayName("PartnerId 정상 생성")
        void shouldCreatePartnerIdSuccessfully() {
            // when
            PartnerId partnerId = PartnerId.of(1L);

            // then
            assertThat(partnerId.value()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("PropertyId")
    class PropertyIdTest {

        @Test
        @DisplayName("PropertyId null 허용 — isNew() = true")
        void shouldAllowNullPropertyId() {
            // when
            PropertyId propertyId = PropertyId.of(null);

            // then
            assertThat(propertyId.isNew()).isTrue();
        }
    }

    @Nested
    @DisplayName("RoomTypeId")
    class RoomTypeIdTest {

        @Test
        @DisplayName("RoomTypeId null 허용 — isNew() = true")
        void shouldAllowNullRoomTypeId() {
            // when
            RoomTypeId roomTypeId = RoomTypeId.of(null);

            // then
            assertThat(roomTypeId.isNew()).isTrue();
        }
    }

    @Nested
    @DisplayName("BrandName")
    class BrandNameTest {

        @Test
        @DisplayName("BrandName 빈 값 실패")
        void shouldFailWhenBrandNameIsBlank() {
            assertThatThrownBy(() -> BrandName.of(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("브랜드명은 필수");
        }

        @Test
        @DisplayName("BrandName null 실패")
        void shouldFailWhenBrandNameIsNull() {
            assertThatThrownBy(() -> BrandName.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("브랜드명은 필수");
        }

        @Test
        @DisplayName("BrandName 정상 생성")
        void shouldCreateBrandNameSuccessfully() {
            BrandName name = BrandName.of("테스트 브랜드");
            assertThat(name.value()).isEqualTo("테스트 브랜드");
        }
    }

    @Nested
    @DisplayName("PropertyTypeCode")
    class PropertyTypeCodeTest {

        @Test
        @DisplayName("PropertyTypeCode 빈 값 실패")
        void shouldFailWhenPropertyTypeCodeIsBlank() {
            assertThatThrownBy(() -> PropertyTypeCode.of(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("숙소 유형 코드는 필수");
        }

        @Test
        @DisplayName("PropertyTypeCode null 실패")
        void shouldFailWhenPropertyTypeCodeIsNull() {
            assertThatThrownBy(() -> PropertyTypeCode.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("숙소 유형 코드는 필수");
        }

        @Test
        @DisplayName("PropertyTypeCode 정상 생성")
        void shouldCreatePropertyTypeCodeSuccessfully() {
            PropertyTypeCode code = PropertyTypeCode.of("HOTEL");
            assertThat(code.value()).isEqualTo("HOTEL");
        }
    }
}
