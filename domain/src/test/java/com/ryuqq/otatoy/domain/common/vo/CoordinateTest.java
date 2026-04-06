package com.ryuqq.otatoy.domain.common.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CoordinateTest {

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("유효한 좌표로 생성할 수 있다")
        void shouldCreateWithValidCoordinate() {
            Coordinate coord = Coordinate.of(37.5665, 126.9780);
            assertThat(coord.latitude()).isEqualTo(37.5665);
            assertThat(coord.longitude()).isEqualTo(126.9780);
        }

        @Test
        @DisplayName("위도 경계값 -90으로 생성할 수 있다")
        void shouldCreateWithMinLatitude() {
            Coordinate coord = Coordinate.of(-90, 0);
            assertThat(coord.latitude()).isEqualTo(-90);
        }

        @Test
        @DisplayName("위도 경계값 90으로 생성할 수 있다")
        void shouldCreateWithMaxLatitude() {
            Coordinate coord = Coordinate.of(90, 0);
            assertThat(coord.latitude()).isEqualTo(90);
        }

        @Test
        @DisplayName("경도 경계값 -180으로 생성할 수 있다")
        void shouldCreateWithMinLongitude() {
            Coordinate coord = Coordinate.of(0, -180);
            assertThat(coord.longitude()).isEqualTo(-180);
        }

        @Test
        @DisplayName("경도 경계값 180으로 생성할 수 있다")
        void shouldCreateWithMaxLongitude() {
            Coordinate coord = Coordinate.of(0, 180);
            assertThat(coord.longitude()).isEqualTo(180);
        }

        @Test
        @DisplayName("위도가 90 초과이면 예외가 발생한다")
        void shouldThrowWhenLatitudeOver90() {
            assertThatThrownBy(() -> Coordinate.of(90.1, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("위도 범위");
        }

        @Test
        @DisplayName("위도가 -90 미만이면 예외가 발생한다")
        void shouldThrowWhenLatitudeUnderMinus90() {
            assertThatThrownBy(() -> Coordinate.of(-90.1, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("위도 범위");
        }

        @Test
        @DisplayName("경도가 180 초과이면 예외가 발생한다")
        void shouldThrowWhenLongitudeOver180() {
            assertThatThrownBy(() -> Coordinate.of(0, 180.1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("경도 범위");
        }

        @Test
        @DisplayName("경도가 -180 미만이면 예외가 발생한다")
        void shouldThrowWhenLongitudeUnderMinus180() {
            assertThatThrownBy(() -> Coordinate.of(0, -180.1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("경도 범위");
        }
    }

    @Nested
    @DisplayName("동등성 검증")
    class Equality {

        @Test
        @DisplayName("같은 좌표는 동등하다")
        void sameValueShouldBeEqual() {
            assertThat(Coordinate.of(37.5665, 126.9780))
                    .isEqualTo(Coordinate.of(37.5665, 126.9780));
        }
    }
}
