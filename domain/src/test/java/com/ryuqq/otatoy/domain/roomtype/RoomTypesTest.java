package com.ryuqq.otatoy.domain.roomtype;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoomTypesTest {

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("null 리스트로 생성 시 빈 컬렉션")
        void shouldCreateEmptyFromNull() {
            RoomTypes roomTypes = RoomTypes.from(null);
            assertThat(roomTypes.isEmpty()).isTrue();
            assertThat(roomTypes.size()).isZero();
        }

        @Test
        @DisplayName("빈 리스트로 생성 시 빈 컬렉션")
        void shouldCreateEmptyFromEmptyList() {
            RoomTypes roomTypes = RoomTypes.from(List.of());
            assertThat(roomTypes.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("정상 리스트로 생성 성공")
        void shouldCreateSuccessfully() {
            // given
            RoomType a = RoomTypeFixture.reconstitutedRoomTypeWithId(1L);
            RoomType b = RoomTypeFixture.reconstitutedRoomTypeWithId(2L);

            // when
            RoomTypes roomTypes = RoomTypes.from(List.of(a, b));

            // then
            assertThat(roomTypes.size()).isEqualTo(2);
            assertThat(roomTypes.items()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("도메인 로직 - roomTypeIds 추출")
    class RoomTypeIdsExtraction {

        @Test
        @DisplayName("roomTypeIds로 ID 목록 추출")
        void shouldExtractRoomTypeIds() {
            // given
            RoomType a = RoomTypeFixture.reconstitutedRoomTypeWithId(10L);
            RoomType b = RoomTypeFixture.reconstitutedRoomTypeWithId(20L);
            RoomTypes roomTypes = RoomTypes.from(List.of(a, b));

            // when
            List<RoomTypeId> ids = roomTypes.roomTypeIds();

            // then
            assertThat(ids).containsExactly(RoomTypeId.of(10L), RoomTypeId.of(20L));
        }

        @Test
        @DisplayName("빈 컬렉션에서 ID 추출 시 빈 리스트")
        void shouldReturnEmptyIdsFromEmptyCollection() {
            RoomTypes roomTypes = RoomTypes.from(List.of());
            assertThat(roomTypes.roomTypeIds()).isEmpty();
        }
    }
}
