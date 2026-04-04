package com.ryuqq.otatoy.domain.roomtype;

import com.ryuqq.otatoy.domain.roomattribute.BedTypeId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoomTypeBedTest {

    private static final RoomTypeId ROOM_TYPE_ID = RoomTypeId.of(1L);
    private static final Instant NOW = Instant.parse("2026-04-04T00:00:00Z");

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("RoomTypeBed 정상 생성")
        void shouldCreateRoomTypeBedSuccessfully() {
            // when
            RoomTypeBed bed = RoomTypeBed.forNew(ROOM_TYPE_ID, BedTypeId.of(1L), 2, NOW);

            // then
            assertThat(bed).isNotNull();
            assertThat(bed.id()).isNotNull();
            assertThat(bed.id().isNew()).isTrue();
            assertThat(bed.quantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("quantity가 0이면 생성 실패 (회귀 방지)")
        void shouldFailWhenQuantityIsZero() {
            assertThatThrownBy(() -> RoomTypeBed.forNew(ROOM_TYPE_ID, BedTypeId.of(1L), 0, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("침대 수량은 1개 이상");
        }

        @Test
        @DisplayName("quantity가 음수이면 생성 실패")
        void shouldFailWhenQuantityIsNegative() {
            assertThatThrownBy(() -> RoomTypeBed.forNew(ROOM_TYPE_ID, BedTypeId.of(1L), -1, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("침대 수량은 1개 이상");
        }

        @Test
        @DisplayName("bedTypeId가 null이면 생성 실패")
        void shouldFailWhenBedTypeIdIsNull() {
            assertThatThrownBy(() -> RoomTypeBed.forNew(ROOM_TYPE_ID, null, 2, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("침대 유형 ID는 필수");
        }
    }
}
