package com.ryuqq.otatoy.application.reservation.facade;

import com.ryuqq.otatoy.application.common.factory.TimeProvider;
import com.ryuqq.otatoy.application.inventory.manager.InventoryCommandManager;
import com.ryuqq.otatoy.application.reservation.port.out.ReservationCommandPort;
import com.ryuqq.otatoy.domain.reservation.Reservation;
import com.ryuqq.otatoy.domain.reservation.ReservationAlreadyCancelledException;
import com.ryuqq.otatoy.domain.reservation.ReservationFixture;
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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

/**
 * CancelReservationFacade 단위 테스트.
 * ReservationCommandPort, InventoryCommandManager, TimeProvider를
 * Mock으로 대체하여 트랜잭션 내 원자적 취소 처리를 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class CancelReservationFacadeTest {

    @Mock
    ReservationCommandPort reservationCommandPort;

    @Mock
    InventoryCommandManager inventoryCommandManager;

    @Mock
    TimeProvider timeProvider;

    @InjectMocks
    CancelReservationFacade facade;

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("CONFIRMED 예약 취소 시 예외 없이 처리된다")
        void shouldCancelConfirmedReservation() {
            // given
            Reservation reservation = ReservationFixture.confirmedReservation();
            RoomTypeId roomTypeId = RoomTypeId.of(1L);
            List<LocalDate> stayDates = List.of(LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 11));
            Instant fixedNow = Instant.parse("2026-04-06T10:00:00Z");
            String cancelReason = "고객 요청 취소";

            given(timeProvider.now()).willReturn(fixedNow);
            given(reservationCommandPort.persist(reservation)).willReturn(1L);
            willDoNothing().given(inventoryCommandManager).incrementAvailable(roomTypeId, stayDates);

            // when & then
            assertThatCode(() -> facade.cancel(reservation, cancelReason, roomTypeId, stayDates))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("cancel -> persist -> incrementAvailable 순서로 호출된다")
        void shouldCallInCorrectOrder() {
            // given
            Reservation reservation = ReservationFixture.confirmedReservation();
            RoomTypeId roomTypeId = RoomTypeId.of(1L);
            List<LocalDate> stayDates = List.of(LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 11));
            Instant fixedNow = Instant.parse("2026-04-06T10:00:00Z");

            given(timeProvider.now()).willReturn(fixedNow);
            given(reservationCommandPort.persist(reservation)).willReturn(1L);
            willDoNothing().given(inventoryCommandManager).incrementAvailable(roomTypeId, stayDates);

            // when
            facade.cancel(reservation, "고객 요청 취소", roomTypeId, stayDates);

            // then -- cancel은 도메인 메서드라 검증 불가, persist -> increment 순서 검증
            InOrder inOrder = inOrder(reservationCommandPort, inventoryCommandManager);
            inOrder.verify(reservationCommandPort).persist(reservation);
            inOrder.verify(inventoryCommandManager).incrementAvailable(roomTypeId, stayDates);
        }
    }

    @Nested
    @DisplayName("실패 흐름")
    class Failure {

        @Test
        @DisplayName("이미 취소된 예약이면 ReservationAlreadyCancelledException이 전파된다")
        void shouldThrowWhenAlreadyCancelled() {
            // given
            Reservation reservation = ReservationFixture.cancelledReservation();
            RoomTypeId roomTypeId = RoomTypeId.of(1L);
            List<LocalDate> stayDates = List.of(LocalDate.of(2026, 4, 10));
            Instant fixedNow = Instant.parse("2026-04-06T10:00:00Z");

            given(timeProvider.now()).willReturn(fixedNow);

            // when & then
            assertThatThrownBy(() -> facade.cancel(reservation, "재취소 시도", roomTypeId, stayDates))
                .isInstanceOf(ReservationAlreadyCancelledException.class);
        }

        @Test
        @DisplayName("도메인 cancel() 실패 시 persist와 재고 복구가 호출되지 않는다")
        void shouldNotPersistOrRestoreWhenCancelFails() {
            // given
            Reservation reservation = ReservationFixture.cancelledReservation();
            RoomTypeId roomTypeId = RoomTypeId.of(1L);
            List<LocalDate> stayDates = List.of(LocalDate.of(2026, 4, 10));
            Instant fixedNow = Instant.parse("2026-04-06T10:00:00Z");

            given(timeProvider.now()).willReturn(fixedNow);

            // when
            try {
                facade.cancel(reservation, "재취소 시도", roomTypeId, stayDates);
            } catch (ReservationAlreadyCancelledException ignored) {
                // 예외는 무시 -- 호출 검증이 목적
            }

            // then
            then(reservationCommandPort).should(never()).persist(any());
            then(inventoryCommandManager).should(never()).incrementAvailable(any(), anyList());
        }
    }
}
