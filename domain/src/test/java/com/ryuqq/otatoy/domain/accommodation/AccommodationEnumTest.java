package com.ryuqq.otatoy.domain.accommodation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Accommodation Enum 검증")
class AccommodationEnumTest {

    @Nested
    @DisplayName("AmenityType")
    class AmenityTypeTest {

        @Test
        @DisplayName("각 편의시설 유형은 올바른 displayName을 가진다")
        void shouldHaveCorrectDisplayNames() {
            assertThat(AmenityType.PARKING.displayName()).isEqualTo("주차장");
            assertThat(AmenityType.POOL.displayName()).isEqualTo("수영장");
            assertThat(AmenityType.FITNESS.displayName()).isEqualTo("피트니스");
            assertThat(AmenityType.WIFI.displayName()).isEqualTo("와이파이");
            assertThat(AmenityType.AIR_CONDITIONING.displayName()).isEqualTo("에어컨");
            assertThat(AmenityType.OTHER.displayName()).isEqualTo("기타");
        }

        @Test
        @DisplayName("편의시설 유형의 총 개수가 올바르다")
        void shouldHaveCorrectCount() {
            assertThat(AmenityType.values()).hasSize(28);
        }
    }

    @Nested
    @DisplayName("PhotoType")
    class PhotoTypeTest {

        @Test
        @DisplayName("각 사진 유형은 올바른 displayName을 가진다")
        void shouldHaveCorrectDisplayNames() {
            assertThat(PhotoType.EXTERIOR.displayName()).isEqualTo("외관");
            assertThat(PhotoType.LOBBY.displayName()).isEqualTo("로비");
            assertThat(PhotoType.ROOM.displayName()).isEqualTo("객실");
            assertThat(PhotoType.BATHROOM.displayName()).isEqualTo("욕실");
            assertThat(PhotoType.VIEW.displayName()).isEqualTo("전망");
            assertThat(PhotoType.FACILITY.displayName()).isEqualTo("시설");
            assertThat(PhotoType.RESTAURANT.displayName()).isEqualTo("레스토랑");
            assertThat(PhotoType.POOL.displayName()).isEqualTo("수영장");
            assertThat(PhotoType.OTHER.displayName()).isEqualTo("기타");
        }

        @Test
        @DisplayName("사진 유형의 총 개수가 9개이다")
        void shouldHaveNineTypes() {
            assertThat(PhotoType.values()).hasSize(9);
        }
    }
}
