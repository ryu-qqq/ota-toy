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

        @Test
        @DisplayName("bedTypeId의 value가 null이면 생성 실패")
        void shouldFailWhenBedTypeIdValueIsNull() {
            assertThatThrownBy(() -> RoomTypeBed.forNew(ROOM_TYPE_ID, BedTypeId.of(null), 2, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("침대 유형 ID는 필수");
        }
    }

    @Nested
    @DisplayName("Pending 패턴")
    class PendingPattern {

        @Test
        @DisplayName("forPending으로 부모 ID 없이 생성 성공")
        void shouldCreatePendingBedWithoutRoomTypeId() {
            // when
            RoomTypeBed bed = RoomTypeBed.forPending(BedTypeId.of(1L), 2, NOW);

            // then
            assertThat(bed.roomTypeId()).isNull();
            assertThat(bed.bedTypeId()).isEqualTo(BedTypeId.of(1L));
            assertThat(bed.quantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("forPending에서도 quantity 0이면 실패")
        void shouldFailPendingWhenQuantityIsZero() {
            assertThatThrownBy(() -> RoomTypeBed.forPending(BedTypeId.of(1L), 0, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("침대 수량은 1개 이상");
        }

        @Test
        @DisplayName("forPending에서도 bedTypeId null이면 실패")
        void shouldFailPendingWhenBedTypeIdIsNull() {
            assertThatThrownBy(() -> RoomTypeBed.forPending(null, 2, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("침대 유형 ID는 필수");
        }

        @Test
        @DisplayName("withRoomTypeId로 부모 ID 할당 후 원본 불변")
        void shouldAssignRoomTypeIdImmutably() {
            // given
            RoomTypeBed pending = RoomTypeBed.forPending(BedTypeId.of(1L), 2, NOW);
            RoomTypeId newRoomTypeId = RoomTypeId.of(100L);

            // when
            RoomTypeBed assigned = pending.withRoomTypeId(newRoomTypeId);

            // then — 새 객체에 roomTypeId 할당됨
            assertThat(assigned.roomTypeId()).isEqualTo(newRoomTypeId);
            assertThat(assigned.bedTypeId()).isEqualTo(BedTypeId.of(1L));
            assertThat(assigned.quantity()).isEqualTo(2);

            // then — 원본은 변경되지 않는다
            assertThat(pending.roomTypeId()).isNull();
        }
    }

    @Nested
    @DisplayName("동등성 검증")
    class Equality {

        @Test
        @DisplayName("같은 ID의 RoomTypeBed는 동등하다")
        void shouldBeEqualWithSameId() {
            RoomTypeBed a = RoomTypeBed.reconstitute(
                    RoomTypeBedId.of(1L), ROOM_TYPE_ID, BedTypeId.of(1L), 1, NOW, NOW
            );
            RoomTypeBed b = RoomTypeBed.reconstitute(
                    RoomTypeBedId.of(1L), ROOM_TYPE_ID, BedTypeId.of(2L), 3, NOW, NOW
            );
            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("다른 ID의 RoomTypeBed는 동등하지 않다")
        void shouldNotBeEqualWithDifferentId() {
            RoomTypeBed a = RoomTypeBed.reconstitute(
                    RoomTypeBedId.of(1L), ROOM_TYPE_ID, BedTypeId.of(1L), 1, NOW, NOW
            );
            RoomTypeBed b = RoomTypeBed.reconstitute(
                    RoomTypeBedId.of(2L), ROOM_TYPE_ID, BedTypeId.of(1L), 1, NOW, NOW
            );
            assertThat(a).isNotEqualTo(b);
        }
    }
}
