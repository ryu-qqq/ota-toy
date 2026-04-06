package com.ryuqq.otatoy.domain.roomtype;

import com.ryuqq.otatoy.domain.roomattribute.ViewTypeId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoomTypeViewTest {

    private static final RoomTypeId ROOM_TYPE_ID = RoomTypeId.of(1L);
    private static final Instant NOW = Instant.parse("2026-04-04T00:00:00Z");

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("RoomTypeView 정상 생성")
        void shouldCreateRoomTypeViewSuccessfully() {
            // when
            RoomTypeView view = RoomTypeView.forNew(ROOM_TYPE_ID, ViewTypeId.of(1L), NOW);

            // then
            assertThat(view).isNotNull();
            assertThat(view.id().isNew()).isTrue();
            assertThat(view.roomTypeId()).isEqualTo(ROOM_TYPE_ID);
            assertThat(view.viewTypeId()).isEqualTo(ViewTypeId.of(1L));
            assertThat(view.createdAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("viewTypeId가 null이면 생성 실패")
        void shouldFailWhenViewTypeIdIsNull() {
            assertThatThrownBy(() -> RoomTypeView.forNew(ROOM_TYPE_ID, null, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("전망 유형 ID는 필수입니다");
        }

        @Test
        @DisplayName("viewTypeId의 value가 null이면 생성 실패")
        void shouldFailWhenViewTypeIdValueIsNull() {
            assertThatThrownBy(() -> RoomTypeView.forNew(ROOM_TYPE_ID, ViewTypeId.of(null), NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("전망 유형 ID는 필수입니다");
        }

        @Test
        @DisplayName("roomTypeId가 null이면 forNew에서 생성 실패")
        void shouldFailWhenRoomTypeIdIsNull() {
            assertThatThrownBy(() -> RoomTypeView.forNew(null, ViewTypeId.of(1L), NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("객실 유형 ID는 필수입니다");
        }
    }

    @Nested
    @DisplayName("Pending 패턴")
    class PendingPattern {

        @Test
        @DisplayName("forPending으로 부모 ID 없이 생성 성공")
        void shouldCreatePendingViewWithoutRoomTypeId() {
            // when
            RoomTypeView view = RoomTypeView.forPending(ViewTypeId.of(1L), NOW);

            // then
            assertThat(view.roomTypeId()).isNull();
            assertThat(view.viewTypeId()).isEqualTo(ViewTypeId.of(1L));
        }

        @Test
        @DisplayName("forPending에서도 viewTypeId null이면 실패")
        void shouldFailPendingWhenViewTypeIdIsNull() {
            assertThatThrownBy(() -> RoomTypeView.forPending(null, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("전망 유형 ID는 필수입니다");
        }

        @Test
        @DisplayName("withRoomTypeId로 부모 ID 할당 후 원본 불변")
        void shouldAssignRoomTypeIdImmutably() {
            // given
            RoomTypeView pending = RoomTypeView.forPending(ViewTypeId.of(1L), NOW);
            RoomTypeId newRoomTypeId = RoomTypeId.of(100L);

            // when
            RoomTypeView assigned = pending.withRoomTypeId(newRoomTypeId);

            // then
            assertThat(assigned.roomTypeId()).isEqualTo(newRoomTypeId);
            assertThat(assigned.viewTypeId()).isEqualTo(ViewTypeId.of(1L));
            // 원본은 변경되지 않는다
            assertThat(pending.roomTypeId()).isNull();
        }
    }

    @Nested
    @DisplayName("동등성 검증")
    class Equality {

        @Test
        @DisplayName("같은 ID의 RoomTypeView는 동등하다")
        void shouldBeEqualWithSameId() {
            RoomTypeView a = RoomTypeView.reconstitute(
                    RoomTypeViewId.of(1L), ROOM_TYPE_ID, ViewTypeId.of(1L), NOW, NOW
            );
            RoomTypeView b = RoomTypeView.reconstitute(
                    RoomTypeViewId.of(1L), ROOM_TYPE_ID, ViewTypeId.of(2L), NOW, NOW
            );
            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("다른 ID의 RoomTypeView는 동등하지 않다")
        void shouldNotBeEqualWithDifferentId() {
            RoomTypeView a = RoomTypeView.reconstitute(
                    RoomTypeViewId.of(1L), ROOM_TYPE_ID, ViewTypeId.of(1L), NOW, NOW
            );
            RoomTypeView b = RoomTypeView.reconstitute(
                    RoomTypeViewId.of(2L), ROOM_TYPE_ID, ViewTypeId.of(1L), NOW, NOW
            );
            assertThat(a).isNotEqualTo(b);
        }
    }
}
