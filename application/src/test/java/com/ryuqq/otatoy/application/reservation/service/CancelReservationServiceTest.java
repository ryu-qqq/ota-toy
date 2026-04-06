package com.ryuqq.otatoy.application.reservation.service;

import com.ryuqq.otatoy.application.inventory.manager.InventoryClientManager;
import com.ryuqq.otatoy.application.reservation.dto.command.CancelReservationCommand;
import com.ryuqq.otatoy.application.reservation.facade.CancelReservationFacade;
import com.ryuqq.otatoy.application.reservation.manager.ReservationReadManager;
import com.ryuqq.otatoy.application.reservation.manager.ReservationSessionReadManager;
import com.ryuqq.otatoy.domain.reservation.Reservation;
import com.ryuqq.otatoy.domain.reservation.ReservationFixture;
import com.ryuqq.otatoy.domain.reservation.ReservationId;
import com.ryuqq.otatoy.domain.reservation.ReservationNotFoundException;
import com.ryuqq.otatoy.domain.reservation.ReservationSession;
import com.ryuqq.otatoy.domain.reservation.ReservationSessionFixture;
import com.ryuqq.otatoy.domain.reservation.ReservationSessionNotFoundException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

/**
 * CancelReservationService 단위 테스트.
 * ReservationReadManager, SessionReadManager, CancelReservationFacade, InventoryClientManager를
 * Mock으로 대체하여 예약 취소 오케스트레이션을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class CancelReservationServiceTest {

    @Mock
    ReservationReadManager reservationReadManager;

    @Mock
    ReservationSessionReadManager sessionReadManager;

    @Mock
    CancelReservationFacade cancelReservationFacade;

    @Mock
    InventoryClientManager inventoryClientManager;

    @InjectMocks
    CancelReservationService service;

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("예약 취소 성공 시 Facade와 Redis 재고 복구가 모두 호출된다")
        void shouldCancelAndRestoreRedisStock() {
            // given
            CancelReservationCommand command = new CancelReservationCommand(
                ReservationId.of(1L), "고객 요청 취소"
            );
            Reservation reservation = ReservationFixture.confirmedReservation();
            ReservationSession session = ReservationSessionFixture.confirmedSession(1L, 1L);

            given(reservationReadManager.getById(command.reservationId())).willReturn(reservation);
            given(sessionReadManager.getByReservationId(reservation.id().value())).willReturn(session);
            willDoNothing().given(cancelReservationFacade)
                .cancel(reservation, command.cancelReason(), session.roomTypeId(), session.stayDates());
            willDoNothing().given(inventoryClientManager)
                .incrementStock(session.roomTypeId(), session.stayDates());

            // when
            service.execute(command);

            // then
            then(cancelReservationFacade).should()
                .cancel(reservation, command.cancelReason(), session.roomTypeId(), session.stayDates());
            then(inventoryClientManager).should()
                .incrementStock(session.roomTypeId(), session.stayDates());
        }
    }

    @Nested
    @DisplayName("검증 실패 흐름")
    class Failure {

        @Test
        @DisplayName("예약이 존재하지 않으면 ReservationNotFoundException이 전파된다")
        void shouldThrowWhenReservationNotFound() {
            // given
            CancelReservationCommand command = new CancelReservationCommand(
                ReservationId.of(999L), "고객 요청 취소"
            );
            given(reservationReadManager.getById(command.reservationId()))
                .willThrow(new ReservationNotFoundException());

            // when & then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(ReservationNotFoundException.class);
        }

        @Test
        @DisplayName("예약 세션이 존재하지 않으면 ReservationSessionNotFoundException이 전파된다")
        void shouldThrowWhenSessionNotFound() {
            // given
            CancelReservationCommand command = new CancelReservationCommand(
                ReservationId.of(1L), "고객 요청 취소"
            );
            Reservation reservation = ReservationFixture.confirmedReservation();

            given(reservationReadManager.getById(command.reservationId())).willReturn(reservation);
            given(sessionReadManager.getByReservationId(reservation.id().value()))
                .willThrow(new ReservationSessionNotFoundException());

            // when & then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(ReservationSessionNotFoundException.class);
        }

        @Test
        @DisplayName("예약 조회 실패 시 Facade와 InventoryClientManager는 호출되지 않는다")
        void shouldNotCallDownstreamWhenReservationNotFound() {
            // given
            CancelReservationCommand command = new CancelReservationCommand(
                ReservationId.of(999L), "고객 요청 취소"
            );
            given(reservationReadManager.getById(command.reservationId()))
                .willThrow(new ReservationNotFoundException());

            // when
            try {
                service.execute(command);
            } catch (ReservationNotFoundException ignored) {
                // 예외는 무시 -- 호출 검증이 목적
            }

            // then
            then(cancelReservationFacade).should(never()).cancel(any(), anyString(), any(), anyList());
            then(inventoryClientManager).should(never()).incrementStock(any(), anyList());
        }
    }

    @Nested
    @DisplayName("호출 순서 검증")
    class InvocationOrder {

        @Test
        @DisplayName("예약조회 -> 세션조회 -> DB취소(Facade) -> Redis복구 순서로 호출된다")
        void shouldCallInCorrectOrder() {
            // given
            CancelReservationCommand command = new CancelReservationCommand(
                ReservationId.of(1L), "고객 요청 취소"
            );
            Reservation reservation = ReservationFixture.confirmedReservation();
            ReservationSession session = ReservationSessionFixture.confirmedSession(1L, 1L);

            given(reservationReadManager.getById(command.reservationId())).willReturn(reservation);
            given(sessionReadManager.getByReservationId(reservation.id().value())).willReturn(session);
            willDoNothing().given(cancelReservationFacade)
                .cancel(reservation, command.cancelReason(), session.roomTypeId(), session.stayDates());
            willDoNothing().given(inventoryClientManager)
                .incrementStock(session.roomTypeId(), session.stayDates());

            // when
            service.execute(command);

            // then
            InOrder inOrder = inOrder(reservationReadManager, sessionReadManager,
                cancelReservationFacade, inventoryClientManager);
            inOrder.verify(reservationReadManager).getById(command.reservationId());
            inOrder.verify(sessionReadManager).getByReservationId(reservation.id().value());
            inOrder.verify(cancelReservationFacade)
                .cancel(reservation, command.cancelReason(), session.roomTypeId(), session.stayDates());
            inOrder.verify(inventoryClientManager)
                .incrementStock(session.roomTypeId(), session.stayDates());
        }
    }
}
