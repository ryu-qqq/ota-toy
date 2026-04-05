package com.ryuqq.otatoy.domain.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationLineIdTest {

    @Test
    @DisplayName("of()로 생성한 ID는 값이 설정된다")
    void shouldCreateWithValue() {
        ReservationLineId id = ReservationLineId.of(42L);

        assertThat(id.value()).isEqualTo(42L);
        assertThat(id.isNew()).isFalse();
    }

    @Test
    @DisplayName("forNew()로 생성한 ID는 null이고 isNew()는 true다")
    void shouldCreateNewWithNullValue() {
        ReservationLineId id = ReservationLineId.forNew();

        assertThat(id.value()).isNull();
        assertThat(id.isNew()).isTrue();
    }

    @Test
    @DisplayName("같은 값의 ReservationLineId는 동등하다")
    void shouldBeEqualWithSameValue() {
        ReservationLineId id1 = ReservationLineId.of(1L);
        ReservationLineId id2 = ReservationLineId.of(1L);

        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }
}
