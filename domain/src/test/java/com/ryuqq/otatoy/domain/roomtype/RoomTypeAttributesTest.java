package com.ryuqq.otatoy.domain.roomtype;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoomTypeAttributesTest {

    private static final RoomTypeId ROOM_TYPE_ID = RoomTypeId.of(1L);
    private static final Instant NOW = Instant.parse("2026-04-04T00:00:00Z");

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("정상 속성 리스트로 생성 성공")
        void shouldCreateAttributesSuccessfully() {
            // given
            List<RoomTypeAttribute> items = List.of(
                    RoomTypeAttribute.forNew(ROOM_TYPE_ID, "floor", "12층", NOW),
                    RoomTypeAttribute.forNew(ROOM_TYPE_ID, "direction", "남향", NOW)
            );

            // when
            RoomTypeAttributes attrs = RoomTypeAttributes.forNew(items);

            // then
            assertThat(attrs.size()).isEqualTo(2);
            assertThat(attrs.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("null 리스트로 생성 시 빈 컬렉션")
        void shouldCreateEmptyAttributesFromNull() {
            RoomTypeAttributes attrs = RoomTypeAttributes.forNew(null);
            assertThat(attrs.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("빈 리스트로 생성 시 빈 컬렉션")
        void shouldCreateEmptyAttributesFromEmptyList() {
            RoomTypeAttributes attrs = RoomTypeAttributes.forNew(List.of());
            assertThat(attrs.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("불변식 검증 - 속성 키 중복")
    class DuplicateValidation {

        @Test
        @DisplayName("같은 attributeKey 중복 시 생성 실패")
        void shouldFailWhenDuplicateKey() {
            // given
            List<RoomTypeAttribute> items = List.of(
                    RoomTypeAttribute.forNew(ROOM_TYPE_ID, "floor", "12층", NOW),
                    RoomTypeAttribute.forNew(ROOM_TYPE_ID, "floor", "15층", NOW)
            );

            // when & then
            assertThatThrownBy(() -> RoomTypeAttributes.forNew(items))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("객실 속성 키가 중복됩니다");
        }

        @Test
        @DisplayName("reconstitute는 중복 검증을 수행하지 않는다")
        void shouldNotValidateDuplicateOnReconstitute() {
            // given
            List<RoomTypeAttribute> items = List.of(
                    RoomTypeAttribute.reconstitute(RoomTypeAttributeId.of(1L), ROOM_TYPE_ID, "floor", "12층", NOW, NOW),
                    RoomTypeAttribute.reconstitute(RoomTypeAttributeId.of(2L), ROOM_TYPE_ID, "floor", "15층", NOW, NOW)
            );

            // when
            RoomTypeAttributes attrs = RoomTypeAttributes.reconstitute(items);

            // then
            assertThat(attrs.size()).isEqualTo(2);
        }
    }
}
