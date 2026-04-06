package com.ryuqq.otatoy.application.reservation.facade;

import com.ryuqq.otatoy.application.common.factory.TimeProvider;
import com.ryuqq.otatoy.application.inventory.manager.InventoryCommandManager;
import com.ryuqq.otatoy.application.reservation.port.out.ReservationCommandPort;
import com.ryuqq.otatoy.application.reservation.port.out.ReservationSessionCommandPort;
import com.ryuqq.otatoy.domain.inventory.InventoryExhaustedException;
import com.ryuqq.otatoy.domain.reservation.Reservation;
import com.ryuqq.otatoy.domain.reservation.ReservationFixture;
import com.ryuqq.otatoy.domain.reservation.ReservationSession;
import com.ryuqq.otatoy.domain.reservation.ReservationSessionFixture;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

/**
 * ReservationPersistenceFacade 단위 테스트.
 * ReservationCommandPort, SessionCommandPort, InventoryCommandManager, TimeProvider를
 * Mock으로 대체하여 트랜잭션 내 원자적 처리를 검증한다.
 *
 * 참고: Facade는 Port를 직접 의존하므로 Port를 Mock한다 (Facade는 Manager가 아님).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class ReservationPersistenceFacadeTest {

    @Mock
    ReservationCommandPort reservationCommandPort;

    @Mock
    ReservationSessionCommandPort sessionCommandPort;

    @Mock
    InventoryCommandManager inventoryCommandManager;

    @Mock
    TimeProvider timeProvider;

    @InjectMocks
    ReservationPersistenceFacade facade;

    @Nested
    @DisplayName("persist() - 예약 저장 + DB 재고 차감")
    class PersistTest {

        @Test
        @DisplayName("예약 저장 성공 시 reservationId를 반환한다")
        void shouldPersistAndReturnId() {
            // given
            Reservation reservation = ReservationFixture.pendingReservation();
            RoomTypeId roomTypeId = RoomTypeId.of(1L);
            List<LocalDate> stayDates = List.of(LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 11));

            willDoNothing().given(inventoryCommandManager).decrementAvailable(roomTypeId, stayDates);
            given(reservationCommandPort.persist(reservation)).willReturn(100L);

            // when
            Long result = facade.persist(reservation, roomTypeId, stayDates);

            // then
            assertThat(result).isEqualTo(100L);
        }

        @Test
        @DisplayName("DB 재고 차감 -> 예약 저장 순서로 호출된다")
        void shouldDecrementBeforePersist() {
            // given
            Reservation reservation = ReservationFixture.pendingReservation();
            RoomTypeId roomTypeId = RoomTypeId.of(1L);
            List<LocalDate> stayDates = List.of(LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 11));

            willDoNothing().given(inventoryCommandManager).decrementAvailable(roomTypeId, stayDates);
            given(reservationCommandPort.persist(reservation)).willReturn(100L);

            // when
            facade.persist(reservation, roomTypeId, stayDates);

            // then
            InOrder inOrder = inOrder(inventoryCommandManager, reservationCommandPort);
            inOrder.verify(inventoryCommandManager).decrementAvailable(roomTypeId, stayDates);
            inOrder.verify(reservationCommandPort).persist(reservation);
        }

        @Test
        @DisplayName("재고 부족 시 InventoryExhaustedException이 전파되고 예약은 저장되지 않는다")
        void shouldThrowWhenInventoryExhausted() {
            // given
            Reservation reservation = ReservationFixture.pendingReservation();
            RoomTypeId roomTypeId = RoomTypeId.of(1L);
            List<LocalDate> stayDates = List.of(LocalDate.of(2026, 4, 10));

            willThrow(new InventoryExhaustedException())
                .given(inventoryCommandManager).decrementAvailable(roomTypeId, stayDates);

            // when & then
            assertThatThrownBy(() -> facade.persist(reservation, roomTypeId, stayDates))
                .isInstanceOf(InventoryExhaustedException.class);

            then(reservationCommandPort).should(never()).persist(any());
        }
    }

    @Nested
    @DisplayName("confirmReservation() - 예약 확정 원자적 처리")
    class ConfirmReservationTest {

        @Test
        @DisplayName("예약 확정 성공 시 reservationId를 반환한다")
        void shouldConfirmAndReturnId() {
            // given
            Reservation reservation = ReservationFixture.pendingReservation();
            ReservationSession session = ReservationSessionFixture.reconstitutedPendingSession(1L);
            // 세션 TTL 10분 이내 (세션 생성: 2026-04-06T00:00:00Z)
            Instant fixedNow = Instant.parse("2026-04-06T00:05:00Z");

            willDoNothing().given(inventoryCommandManager)
                .decrementAvailable(session.roomTypeId(), session.stayDates());
            given(reservationCommandPort.persist(reservation)).willReturn(100L);
            given(timeProvider.now()).willReturn(fixedNow);
            given(sessionCommandPort.persist(session)).willReturn(1L);

            // when
            Long result = facade.confirmReservation(reservation, session);

            // then
            assertThat(result).isEqualTo(100L);
        }

        @Test
        @DisplayName("DB 재고 차감 -> 예약 저장 -> 세션 확정 순서로 호출된다")
        void shouldCallInCorrectOrder() {
            // given
            Reservation reservation = ReservationFixture.pendingReservation();
            ReservationSession session = ReservationSessionFixture.reconstitutedPendingSession(1L);
            // 세션 TTL 10분 이내 (세션 생성: 2026-04-06T00:00:00Z)
            Instant fixedNow = Instant.parse("2026-04-06T00:05:00Z");

            willDoNothing().given(inventoryCommandManager)
                .decrementAvailable(session.roomTypeId(), session.stayDates());
            given(reservationCommandPort.persist(reservation)).willReturn(100L);
            given(timeProvider.now()).willReturn(fixedNow);
            given(sessionCommandPort.persist(session)).willReturn(1L);

            // when
            facade.confirmReservation(reservation, session);

            // then
            InOrder inOrder = inOrder(inventoryCommandManager, reservationCommandPort, sessionCommandPort);
            inOrder.verify(inventoryCommandManager).decrementAvailable(session.roomTypeId(), session.stayDates());
            inOrder.verify(reservationCommandPort).persist(reservation);
            inOrder.verify(sessionCommandPort).persist(session);
        }

        @Test
        @DisplayName("재고 부족 시 예약 저장과 세션 확정이 호출되지 않는다")
        void shouldNotPersistWhenInventoryExhausted() {
            // given
            Reservation reservation = ReservationFixture.pendingReservation();
            ReservationSession session = ReservationSessionFixture.reconstitutedPendingSession(1L);

            willThrow(new InventoryExhaustedException())
                .given(inventoryCommandManager)
                .decrementAvailable(session.roomTypeId(), session.stayDates());

            // when & then
            assertThatThrownBy(() -> facade.confirmReservation(reservation, session))
                .isInstanceOf(InventoryExhaustedException.class);

            then(reservationCommandPort).should(never()).persist(any());
            then(sessionCommandPort).should(never()).persist(any());
        }
    }
}
