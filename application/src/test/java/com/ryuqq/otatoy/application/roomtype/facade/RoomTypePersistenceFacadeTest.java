package com.ryuqq.otatoy.application.roomtype.facade;

import com.ryuqq.otatoy.application.roomtype.dto.RoomTypeBundle;
import com.ryuqq.otatoy.application.roomtype.dto.RoomTypeBundleFixture;
import com.ryuqq.otatoy.application.roomtype.port.out.RoomTypeBedCommandPort;
import com.ryuqq.otatoy.application.roomtype.port.out.RoomTypeCommandPort;
import com.ryuqq.otatoy.application.roomtype.port.out.RoomTypeViewCommandPort;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeBed;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeFixture;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeView;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

/**
 * RoomTypePersistenceFacade 단위 테스트.
 * CommandPort를 Mock으로 대체하여 원자적 저장 로직을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class RoomTypePersistenceFacadeTest {

    @Mock
    RoomTypeCommandPort roomTypeCommandPort;

    @Mock
    RoomTypeBedCommandPort roomTypeBedCommandPort;

    @Mock
    RoomTypeViewCommandPort roomTypeViewCommandPort;

    @InjectMocks
    RoomTypePersistenceFacade facade;

    @Captor
    ArgumentCaptor<List<RoomTypeBed>> bedsCaptor;

    @Captor
    ArgumentCaptor<List<RoomTypeView>> viewsCaptor;

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("RoomType 저장 후 할당된 ID를 반환한다")
        void shouldReturnPersistedRoomTypeId() {
            // given
            var bundle = RoomTypeBundleFixture.aRoomTypeBundle();
            given(roomTypeCommandPort.persist(bundle.roomType())).willReturn(100L);

            // when
            Long result = facade.persist(bundle);

            // then
            assertThat(result).isEqualTo(100L);
        }

        @Test
        @DisplayName("RoomType 저장 후 Bed에 roomTypeId가 할당되어 저장된다")
        void shouldAssignRoomTypeIdToBedsBeforePersist() {
            // given
            var bundle = RoomTypeBundleFixture.aRoomTypeBundle();
            given(roomTypeCommandPort.persist(bundle.roomType())).willReturn(100L);

            // when
            facade.persist(bundle);

            // then
            then(roomTypeBedCommandPort).should().persistAll(bedsCaptor.capture());
            List<RoomTypeBed> savedBeds = bedsCaptor.getValue();
            savedBeds.forEach(bed ->
                assertThat(bed.roomTypeId().value()).isEqualTo(100L)
            );
        }

        @Test
        @DisplayName("RoomType 저장 후 View에 roomTypeId가 할당되어 저장된다")
        void shouldAssignRoomTypeIdToViewsBeforePersist() {
            // given
            var bundle = RoomTypeBundleFixture.aRoomTypeBundle();
            given(roomTypeCommandPort.persist(bundle.roomType())).willReturn(100L);

            // when
            facade.persist(bundle);

            // then
            then(roomTypeViewCommandPort).should().persistAll(viewsCaptor.capture());
            List<RoomTypeView> savedViews = viewsCaptor.getValue();
            savedViews.forEach(view ->
                assertThat(view.roomTypeId().value()).isEqualTo(100L)
            );
        }
    }

    @Nested
    @DisplayName("호출 순서 검증")
    class InvocationOrder {

        @Test
        @DisplayName("RoomType 저장 -> Bed 저장 -> View 저장 순서로 호출된다")
        void shouldPersistInCorrectOrder() {
            // given
            var bundle = RoomTypeBundleFixture.aRoomTypeBundle();
            given(roomTypeCommandPort.persist(bundle.roomType())).willReturn(1L);

            // when
            facade.persist(bundle);

            // then
            InOrder inOrder = inOrder(roomTypeCommandPort, roomTypeBedCommandPort, roomTypeViewCommandPort);
            inOrder.verify(roomTypeCommandPort).persist(bundle.roomType());
            inOrder.verify(roomTypeBedCommandPort).persistAll(any());
            inOrder.verify(roomTypeViewCommandPort).persistAll(any());
        }
    }

    @Nested
    @DisplayName("빈 번들 처리")
    class EmptyBundle {

        @Test
        @DisplayName("침대가 비어있으면 Bed 저장을 호출하지 않는다")
        void shouldNotPersistBedsWhenEmpty() {
            // given
            var bundle = RoomTypeBundleFixture.anEmptyRoomTypeBundle();
            given(roomTypeCommandPort.persist(bundle.roomType())).willReturn(1L);

            // when
            facade.persist(bundle);

            // then
            then(roomTypeBedCommandPort).should(never()).persistAll(any());
        }

        @Test
        @DisplayName("전망이 비어있으면 View 저장을 호출하지 않는다")
        void shouldNotPersistViewsWhenEmpty() {
            // given
            var bundle = RoomTypeBundleFixture.anEmptyRoomTypeBundle();
            given(roomTypeCommandPort.persist(bundle.roomType())).willReturn(1L);

            // when
            facade.persist(bundle);

            // then
            then(roomTypeViewCommandPort).should(never()).persistAll(any());
        }

        @Test
        @DisplayName("침대/전망이 비어있어도 RoomType은 저장된다")
        void shouldAlwaysPersistRoomType() {
            // given
            var bundle = RoomTypeBundleFixture.anEmptyRoomTypeBundle();
            given(roomTypeCommandPort.persist(bundle.roomType())).willReturn(1L);

            // when
            Long result = facade.persist(bundle);

            // then
            assertThat(result).isEqualTo(1L);
            then(roomTypeCommandPort).should().persist(bundle.roomType());
        }
    }
}
