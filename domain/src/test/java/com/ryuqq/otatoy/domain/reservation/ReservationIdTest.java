package com.ryuqq.otatoy.domain.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationIdTest {

    @Nested
    @DisplayName("T-5: ID VO 검증")
    class IdValidation {

        @Test
        @DisplayName("value가 null이면 isNew()은 true를 반환한다")
        void shouldBeNewWhenValueIsNull() {
            ReservationId id = ReservationId.of(null);

            assertThat(id.isNew()).isTrue();
            assertThat(id.value()).isNull();
        }

        @Test
        @DisplayName("value가 있으면 isNew()은 false를 반환한다")
        void shouldNotBeNewWhenValueExists() {
            ReservationId id = ReservationId.of(1L);

            assertThat(id.isNew()).isFalse();
            assertThat(id.value()).isEqualTo(1L);
        }

        @Test
        @DisplayName("같은 value를 가진 ReservationId는 동등하다")
        void shouldBeEqualWithSameValue() {
            ReservationId id1 = ReservationId.of(42L);
            ReservationId id2 = ReservationId.of(42L);

            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }
    }
}
