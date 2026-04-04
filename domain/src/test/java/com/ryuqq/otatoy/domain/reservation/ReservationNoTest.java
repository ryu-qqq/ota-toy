package com.ryuqq.otatoy.domain.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationNoTest {

    @Nested
    @DisplayName("T-5: VO 생성 검증")
    class Creation {

        @Test
        @DisplayName("정상 생성 시 value가 할당된다")
        void shouldCreateSuccessfully() {
            ReservationNo no = ReservationNo.of("RSV-20260404-001");

            assertThat(no.value()).isEqualTo("RSV-20260404-001");
        }

        @Test
        @DisplayName("value가 null이면 생성 실패")
        void shouldFailWhenValueIsNull() {
            assertThatThrownBy(() -> ReservationNo.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("예약 번호는 필수");
        }

        @Test
        @DisplayName("value가 빈 문자열이면 생성 실패")
        void shouldFailWhenValueIsBlank() {
            assertThatThrownBy(() -> ReservationNo.of("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("예약 번호는 필수");
        }
    }

    @Nested
    @DisplayName("T-5: VO 동등성 검증")
    class Equality {

        @Test
        @DisplayName("같은 값을 가진 ReservationNo는 동등하다 (Record 특성)")
        void shouldBeEqualWithSameValue() {
            ReservationNo no1 = ReservationNo.of("RSV-001");
            ReservationNo no2 = ReservationNo.of("RSV-001");

            assertThat(no1).isEqualTo(no2);
            assertThat(no1.hashCode()).isEqualTo(no2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가진 ReservationNo는 동등하지 않다")
        void shouldNotBeEqualWithDifferentValue() {
            ReservationNo no1 = ReservationNo.of("RSV-001");
            ReservationNo no2 = ReservationNo.of("RSV-002");

            assertThat(no1).isNotEqualTo(no2);
        }
    }
}
