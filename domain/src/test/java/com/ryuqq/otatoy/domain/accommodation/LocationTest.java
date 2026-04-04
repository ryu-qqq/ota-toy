package com.ryuqq.otatoy.domain.accommodation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocationTest {

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("Location 정상 생성")
        void shouldCreateLocationSuccessfully() {
            // when
            Location location = Location.of("서울시 강남구", 37.5, 127.0, "강남", "서울");

            // then
            assertThat(location.address()).isEqualTo("서울시 강남구");
            assertThat(location.latitude()).isEqualTo(37.5);
            assertThat(location.longitude()).isEqualTo(127.0);
        }

        @Test
        @DisplayName("위도가 -91이면 생성 실패")
        void shouldFailWhenLatitudeIsBelowMinus90() {
            assertThatThrownBy(() -> Location.of("주소", -91, 127.0, "동네", "지역"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("위도 범위");
        }

        @Test
        @DisplayName("위도가 91이면 생성 실패")
        void shouldFailWhenLatitudeIsAbove90() {
            assertThatThrownBy(() -> Location.of("주소", 91, 127.0, "동네", "지역"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("위도 범위");
        }

        @Test
        @DisplayName("경도가 181이면 생성 실패")
        void shouldFailWhenLongitudeIsAbove180() {
            assertThatThrownBy(() -> Location.of("주소", 37.5, 181, "동네", "지역"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("경도 범위");
        }

        @Test
        @DisplayName("경도가 -181이면 생성 실패")
        void shouldFailWhenLongitudeIsBelowMinus180() {
            assertThatThrownBy(() -> Location.of("주소", 37.5, -181, "동네", "지역"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("경도 범위");
        }

        @Test
        @DisplayName("주소가 빈 값이면 생성 실패")
        void shouldFailWhenAddressIsBlank() {
            assertThatThrownBy(() -> Location.of("", 37.5, 127.0, "동네", "지역"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("주소는 필수");
        }

        @Test
        @DisplayName("경계값 — 위도 90, 경도 180은 성공")
        void shouldSucceedAtBoundaryValues() {
            // when
            Location location = Location.of("주소", 90, 180, "동네", "지역");

            // then
            assertThat(location.latitude()).isEqualTo(90);
            assertThat(location.longitude()).isEqualTo(180);
        }

        @Test
        @DisplayName("경계값 — 위도 -90, 경도 -180은 성공")
        void shouldSucceedAtNegativeBoundaryValues() {
            // when
            Location location = Location.of("주소", -90, -180, "동네", "지역");

            // then
            assertThat(location.latitude()).isEqualTo(-90);
            assertThat(location.longitude()).isEqualTo(-180);
        }
    }
}
