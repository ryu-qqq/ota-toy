package com.ryuqq.otatoy.domain.roomtype;

import com.ryuqq.otatoy.domain.roomattribute.ViewTypeId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoomTypeViewsTest {

    private static final RoomTypeId ROOM_TYPE_ID = RoomTypeId.of(1L);
    private static final Instant NOW = Instant.parse("2026-04-04T00:00:00Z");

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("정상 전망 리스트로 생성 성공")
        void shouldCreateViewsSuccessfully() {
            // given
            List<RoomTypeView> items = List.of(
                    RoomTypeView.forNew(ROOM_TYPE_ID, ViewTypeId.of(1L), NOW),
                    RoomTypeView.forNew(ROOM_TYPE_ID, ViewTypeId.of(2L), NOW)
            );

            // when
            RoomTypeViews views = RoomTypeViews.forNew(items);

            // then
            assertThat(views.size()).isEqualTo(2);
            assertThat(views.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("null 리스트로 생성 시 빈 컬렉션")
        void shouldCreateEmptyViewsFromNull() {
            RoomTypeViews views = RoomTypeViews.forNew(null);
            assertThat(views.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("빈 리스트로 생성 시 빈 컬렉션")
        void shouldCreateEmptyViewsFromEmptyList() {
            RoomTypeViews views = RoomTypeViews.forNew(List.of());
            assertThat(views.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("불변식 검증 - 전망 유형 중복")
    class DuplicateValidation {

        @Test
        @DisplayName("같은 viewTypeId 중복 시 생성 실패")
        void shouldFailWhenDuplicateViewType() {
            // given
            List<RoomTypeView> items = List.of(
                    RoomTypeView.forNew(ROOM_TYPE_ID, ViewTypeId.of(1L), NOW),
                    RoomTypeView.forNew(ROOM_TYPE_ID, ViewTypeId.of(1L), NOW)
            );

            // when & then
            assertThatThrownBy(() -> RoomTypeViews.forNew(items))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("객실 전망 유형이 중복됩니다");
        }

        @Test
        @DisplayName("reconstitute는 중복 검증을 수행하지 않는다")
        void shouldNotValidateDuplicateOnReconstitute() {
            // given
            List<RoomTypeView> items = List.of(
                    RoomTypeView.reconstitute(RoomTypeViewId.of(1L), ROOM_TYPE_ID, ViewTypeId.of(1L), NOW, NOW),
                    RoomTypeView.reconstitute(RoomTypeViewId.of(2L), ROOM_TYPE_ID, ViewTypeId.of(1L), NOW, NOW)
            );

            // when
            RoomTypeViews views = RoomTypeViews.reconstitute(items);

            // then
            assertThat(views.size()).isEqualTo(2);
        }
    }
}
