package com.ryuqq.otatoy.domain.roomtype;

import com.ryuqq.otatoy.domain.roomattribute.BedTypeId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoomTypeBedsTest {

    private static final RoomTypeId ROOM_TYPE_ID = RoomTypeId.of(1L);
    private static final Instant NOW = Instant.parse("2026-04-04T00:00:00Z");

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("정상 침대 리스트로 생성 성공")
        void shouldCreateBedsSuccessfully() {
            // given
            List<RoomTypeBed> items = List.of(
                    RoomTypeBed.forNew(ROOM_TYPE_ID, BedTypeId.of(1L), 1, NOW),
                    RoomTypeBed.forNew(ROOM_TYPE_ID, BedTypeId.of(2L), 2, NOW)
            );

            // when
            RoomTypeBeds beds = RoomTypeBeds.forNew(items);

            // then
            assertThat(beds.size()).isEqualTo(2);
            assertThat(beds.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("null 리스트로 생성 시 빈 컬렉션")
        void shouldCreateEmptyBedsFromNull() {
            RoomTypeBeds beds = RoomTypeBeds.forNew(null);
            assertThat(beds.isEmpty()).isTrue();
            assertThat(beds.size()).isZero();
        }

        @Test
        @DisplayName("빈 리스트로 생성 시 빈 컬렉션")
        void shouldCreateEmptyBedsFromEmptyList() {
            RoomTypeBeds beds = RoomTypeBeds.forNew(List.of());
            assertThat(beds.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("불변식 검증 - 침대 유형 중복")
    class DuplicateValidation {

        @Test
        @DisplayName("같은 bedTypeId 중복 시 생성 실패")
        void shouldFailWhenDuplicateBedType() {
            // given
            List<RoomTypeBed> items = List.of(
                    RoomTypeBed.forNew(ROOM_TYPE_ID, BedTypeId.of(1L), 1, NOW),
                    RoomTypeBed.forNew(ROOM_TYPE_ID, BedTypeId.of(1L), 2, NOW)
            );

            // when & then
            assertThatThrownBy(() -> RoomTypeBeds.forNew(items))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("객실 침대 유형이 중복됩니다");
        }

        @Test
        @DisplayName("reconstitute는 중복 검증을 수행하지 않는다")
        void shouldNotValidateDuplicateOnReconstitute() {
            // given — DB에 이미 저장된 데이터이므로 검증 스킵
            List<RoomTypeBed> items = List.of(
                    RoomTypeBed.reconstitute(RoomTypeBedId.of(1L), ROOM_TYPE_ID, BedTypeId.of(1L), 1, NOW, NOW),
                    RoomTypeBed.reconstitute(RoomTypeBedId.of(2L), ROOM_TYPE_ID, BedTypeId.of(1L), 2, NOW, NOW)
            );

            // when
            RoomTypeBeds beds = RoomTypeBeds.reconstitute(items);

            // then
            assertThat(beds.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("도메인 로직 - totalQuantity")
    class TotalQuantity {

        @Test
        @DisplayName("총 침대 수량 계산")
        void shouldCalculateTotalQuantity() {
            // given
            List<RoomTypeBed> items = List.of(
                    RoomTypeBed.forNew(ROOM_TYPE_ID, BedTypeId.of(1L), 1, NOW),
                    RoomTypeBed.forNew(ROOM_TYPE_ID, BedTypeId.of(2L), 3, NOW)
            );
            RoomTypeBeds beds = RoomTypeBeds.forNew(items);

            // when & then
            assertThat(beds.totalQuantity()).isEqualTo(4);
        }

        @Test
        @DisplayName("빈 컬렉션의 총 수량은 0")
        void shouldReturnZeroForEmptyBeds() {
            RoomTypeBeds beds = RoomTypeBeds.forNew(List.of());
            assertThat(beds.totalQuantity()).isZero();
        }
    }
}
