package com.ryuqq.otatoy.domain.roomtype;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoomTypeAttributeTest {

    private static final RoomTypeId ROOM_TYPE_ID = RoomTypeId.of(1L);
    private static final Instant NOW = Instant.parse("2026-04-04T00:00:00Z");

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("RoomTypeAttribute 정상 생성")
        void shouldCreateAttributeSuccessfully() {
            // when
            RoomTypeAttribute attr = RoomTypeAttribute.forNew(ROOM_TYPE_ID, "floor", "12층", NOW);

            // then
            assertThat(attr).isNotNull();
            assertThat(attr.id().isNew()).isTrue();
            assertThat(attr.roomTypeId()).isEqualTo(ROOM_TYPE_ID);
            assertThat(attr.attributeKey()).isEqualTo("floor");
            assertThat(attr.attributeValue()).isEqualTo("12층");
        }

        @Test
        @DisplayName("attributeKey가 null이면 생성 실패")
        void shouldFailWhenKeyIsNull() {
            assertThatThrownBy(() -> RoomTypeAttribute.forNew(ROOM_TYPE_ID, null, "값", NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("속성 키는 필수입니다");
        }

        @Test
        @DisplayName("attributeKey가 빈 값이면 생성 실패")
        void shouldFailWhenKeyIsBlank() {
            assertThatThrownBy(() -> RoomTypeAttribute.forNew(ROOM_TYPE_ID, "", "값", NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("속성 키는 필수입니다");
        }

        @Test
        @DisplayName("attributeKey가 공백만 있으면 생성 실패")
        void shouldFailWhenKeyIsWhitespace() {
            assertThatThrownBy(() -> RoomTypeAttribute.forNew(ROOM_TYPE_ID, "   ", "값", NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("속성 키는 필수입니다");
        }

        @Test
        @DisplayName("attributeValue가 null이어도 생성 성공")
        void shouldSucceedWhenValueIsNull() {
            RoomTypeAttribute attr = RoomTypeAttribute.forNew(ROOM_TYPE_ID, "hasBalcony", null, NOW);
            assertThat(attr.attributeValue()).isNull();
        }
    }

    @Nested
    @DisplayName("동등성 검증")
    class Equality {

        @Test
        @DisplayName("같은 ID의 RoomTypeAttribute는 동등하다")
        void shouldBeEqualWithSameId() {
            RoomTypeAttribute a = RoomTypeAttribute.reconstitute(
                    RoomTypeAttributeId.of(1L), ROOM_TYPE_ID, "floor", "12층", NOW, NOW
            );
            RoomTypeAttribute b = RoomTypeAttribute.reconstitute(
                    RoomTypeAttributeId.of(1L), ROOM_TYPE_ID, "view", "바다", NOW, NOW
            );
            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("다른 ID의 RoomTypeAttribute는 동등하지 않다")
        void shouldNotBeEqualWithDifferentId() {
            RoomTypeAttribute a = RoomTypeAttribute.reconstitute(
                    RoomTypeAttributeId.of(1L), ROOM_TYPE_ID, "floor", "12층", NOW, NOW
            );
            RoomTypeAttribute b = RoomTypeAttribute.reconstitute(
                    RoomTypeAttributeId.of(2L), ROOM_TYPE_ID, "floor", "12층", NOW, NOW
            );
            assertThat(a).isNotEqualTo(b);
        }
    }
}
