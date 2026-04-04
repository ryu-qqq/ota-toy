package com.ryuqq.otatoy.domain.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationItemIdTest {

    @Nested
    @DisplayName("T-5: ID VO 검증")
    class IdValidation {

        @Test
        @DisplayName("value가 null이면 isNew()은 true를 반환한다")
        void shouldBeNewWhenValueIsNull() {
            ReservationItemId id = ReservationItemId.of(null);

            assertThat(id.isNew()).isTrue();
            assertThat(id.value()).isNull();
        }

        @Test
        @DisplayName("value가 있으면 isNew()은 false를 반환한다")
        void shouldNotBeNewWhenValueExists() {
            ReservationItemId id = ReservationItemId.of(1L);

            assertThat(id.isNew()).isFalse();
            assertThat(id.value()).isEqualTo(1L);
        }

        @Test
        @DisplayName("같은 value를 가진 ReservationItemId는 동등하다")
        void shouldBeEqualWithSameValue() {
            ReservationItemId id1 = ReservationItemId.of(42L);
            ReservationItemId id2 = ReservationItemId.of(42L);

            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("다른 value를 가진 ReservationItemId는 동등하지 않다")
        void shouldNotBeEqualWithDifferentValue() {
            ReservationItemId id1 = ReservationItemId.of(1L);
            ReservationItemId id2 = ReservationItemId.of(2L);

            assertThat(id1).isNotEqualTo(id2);
        }
    }
}
