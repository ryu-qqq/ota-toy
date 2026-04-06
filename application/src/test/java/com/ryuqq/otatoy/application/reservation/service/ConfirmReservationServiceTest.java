package com.ryuqq.otatoy.application.reservation.service;

import com.ryuqq.otatoy.application.reservation.dto.command.ConfirmReservationCommand;
import com.ryuqq.otatoy.application.reservation.dto.command.ConfirmReservationCommandFixture;
import com.ryuqq.otatoy.application.reservation.facade.ReservationPersistenceFacade;
import com.ryuqq.otatoy.application.reservation.factory.ReservationFactory;
import com.ryuqq.otatoy.application.reservation.manager.ReservationSessionReadManager;
import com.ryuqq.otatoy.domain.reservation.Reservation;
import com.ryuqq.otatoy.domain.reservation.ReservationFixture;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

/**
 * ConfirmReservationService 단위 테스트.
 * SessionReadManager, ReservationFactory, ReservationPersistenceFacade를
 * Mock으로 대체하여 예약 확정 오케스트레이션을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class ConfirmReservationServiceTest {

    @Mock
    ReservationSessionReadManager sessionReadManager;

    @Mock
    ReservationFactory reservationFactory;

    @Mock
    ReservationPersistenceFacade reservationPersistenceFacade;

    @InjectMocks
    ConfirmReservationService service;

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("PENDING 세션으로 예약 확정 시 reservationId를 반환한다")
        void shouldConfirmReservationAndReturnId() {
            // given
            ConfirmReservationCommand command = ConfirmReservationCommandFixture.aConfirmReservationCommand();
            ReservationSession session = ReservationSessionFixture.reconstitutedPendingSession(1L);
            Reservation reservation = ReservationFixture.pendingReservation();

            given(sessionReadManager.getById(command.sessionId())).willReturn(session);
            given(reservationFactory.createFromSession(session, command)).willReturn(reservation);
            given(reservationPersistenceFacade.confirmReservation(reservation, session)).willReturn(100L);

            // when
            Long result = service.execute(command);

            // then
            assertThat(result).isEqualTo(100L);
        }

        @Test
        @DisplayName("이미 CONFIRMED 상태인 세션이면 기존 reservationId를 반환한다 (멱등)")
        void shouldReturnExistingReservationIdWhenAlreadyConfirmed() {
            // given
            ConfirmReservationCommand command = ConfirmReservationCommandFixture.aConfirmReservationCommand();
            ReservationSession confirmedSession = ReservationSessionFixture.confirmedSession(1L, 50L);

            given(sessionReadManager.getById(command.sessionId())).willReturn(confirmedSession);

            // when
            Long result = service.execute(command);

            // then
            assertThat(result).isEqualTo(50L);
            then(reservationFactory).shouldHaveNoInteractions();
            then(reservationPersistenceFacade).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("검증 실패 흐름")
    class Failure {

        @Test
        @DisplayName("세션이 존재하지 않으면 ReservationSessionNotFoundException이 전파된다")
        void shouldThrowWhenSessionNotFound() {
            // given
            ConfirmReservationCommand command = ConfirmReservationCommandFixture.aConfirmReservationCommand();
            given(sessionReadManager.getById(command.sessionId()))
                .willThrow(new ReservationSessionNotFoundException());

            // when & then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(ReservationSessionNotFoundException.class);
        }

        @Test
        @DisplayName("세션 조회 실패 시 Factory와 Facade는 호출되지 않는다")
        void shouldNotCallFactoryOrFacadeWhenSessionNotFound() {
            // given
            ConfirmReservationCommand command = ConfirmReservationCommandFixture.aConfirmReservationCommand();
            given(sessionReadManager.getById(command.sessionId()))
                .willThrow(new ReservationSessionNotFoundException());

            // when
            try {
                service.execute(command);
            } catch (ReservationSessionNotFoundException ignored) {
                // 예외는 무시 -- 호출 검증이 목적
            }

            // then
            then(reservationFactory).should(never()).createFromSession(any(), any());
            then(reservationPersistenceFacade).should(never()).confirmReservation(any(), any());
        }
    }

    @Nested
    @DisplayName("호출 순서 검증")
    class InvocationOrder {

        @Test
        @DisplayName("getById -> createFromSession -> confirmReservation 순서로 호출된다")
        void shouldCallInCorrectOrder() {
            // given
            ConfirmReservationCommand command = ConfirmReservationCommandFixture.aConfirmReservationCommand();
            ReservationSession session = ReservationSessionFixture.reconstitutedPendingSession(1L);
            Reservation reservation = ReservationFixture.pendingReservation();

            given(sessionReadManager.getById(command.sessionId())).willReturn(session);
            given(reservationFactory.createFromSession(session, command)).willReturn(reservation);
            given(reservationPersistenceFacade.confirmReservation(reservation, session)).willReturn(100L);

            // when
            service.execute(command);

            // then
            InOrder inOrder = inOrder(sessionReadManager, reservationFactory, reservationPersistenceFacade);
            inOrder.verify(sessionReadManager).getById(command.sessionId());
            inOrder.verify(reservationFactory).createFromSession(session, command);
            inOrder.verify(reservationPersistenceFacade).confirmReservation(reservation, session);
        }
    }
}
