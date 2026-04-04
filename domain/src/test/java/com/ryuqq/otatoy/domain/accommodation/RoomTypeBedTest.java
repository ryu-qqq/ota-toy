package com.ryuqq.otatoy.domain.accommodation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoomTypeBedTest {

    private static final RoomTypeId ROOM_TYPE_ID = RoomTypeId.of(1L);

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("RoomTypeBed 정상 생성")
        void shouldCreateRoomTypeBedSuccessfully() {
            // when
            RoomTypeBed bed = RoomTypeBed.forNew(ROOM_TYPE_ID, BedTypeId.of(1L), 2);

            // then
            assertThat(bed).isNotNull();
            assertThat(bed.id()).isNotNull();
            assertThat(bed.id().isNew()).isTrue();
            assertThat(bed.quantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("quantity가 0이면 생성 실패 (회귀 방지)")
        void shouldFailWhenQuantityIsZero() {
            assertThatThrownBy(() -> RoomTypeBed.forNew(ROOM_TYPE_ID, BedTypeId.of(1L), 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("침대 수량은 1개 이상");
        }

        @Test
        @DisplayName("quantity가 음수이면 생성 실패")
        void shouldFailWhenQuantityIsNegative() {
            assertThatThrownBy(() -> RoomTypeBed.forNew(ROOM_TYPE_ID, BedTypeId.of(1L), -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("침대 수량은 1개 이상");
        }

        @Test
        @DisplayName("bedTypeId가 null이면 생성 실패")
        void shouldFailWhenBedTypeIdIsNull() {
            assertThatThrownBy(() -> RoomTypeBed.forNew(ROOM_TYPE_ID, null, 2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("침대 유형 ID는 필수");
        }
    }
}
