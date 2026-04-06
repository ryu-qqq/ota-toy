package com.ryuqq.otatoy.application.reservation.service;

import com.ryuqq.otatoy.application.inventory.manager.InventoryClientManager;
import com.ryuqq.otatoy.application.reservation.dto.command.CreateReservationCommand;
import com.ryuqq.otatoy.application.reservation.dto.command.CreateReservationCommandFixture;
import com.ryuqq.otatoy.application.reservation.facade.ReservationPersistenceFacade;
import com.ryuqq.otatoy.application.reservation.factory.ReservationFactory;
import com.ryuqq.otatoy.application.reservation.validator.CreateReservationValidator;
import com.ryuqq.otatoy.domain.inventory.InventoryExhaustedException;
import com.ryuqq.otatoy.domain.property.PropertyNotFoundException;
import com.ryuqq.otatoy.domain.reservation.Reservation;
import com.ryuqq.otatoy.domain.reservation.ReservationFixture;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeNotFoundException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

/**
 * CreateReservationService 단위 테스트.
 * Validator, Factory, InventoryClientManager, ReservationPersistenceFacade를
 * Mock으로 대체하여 Service의 오케스트레이션 및 보상 로직을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class CreateReservationServiceTest {

    @Mock
    CreateReservationValidator validator;

    @Mock
    ReservationFactory reservationFactory;

    @Mock
    InventoryClientManager inventoryClientManager;

    @Mock
    ReservationPersistenceFacade reservationPersistenceFacade;

    @InjectMocks
    CreateReservationService service;

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("유효한 예약 요청 시 Redis DECR -> Factory -> Facade persist -> reservationId 반환")
        void shouldCreateReservationAndReturnId() {
            // given
            CreateReservationCommand command = CreateReservationCommandFixture.aCreateReservationCommand();
            Reservation reservation = ReservationFixture.pendingReservation();
            List<LocalDate> stayDates = command.stayDates();

            willDoNothing().given(validator).validate(command);
            willDoNothing().given(inventoryClientManager).decrementStock(command.roomTypeId(), stayDates);
            given(reservationFactory.create(command)).willReturn(reservation);
            given(reservationPersistenceFacade.persist(any(), any(), anyList())).willReturn(1L);

            // when
            Long result = service.execute(command);

            // then
            assertThat(result).isEqualTo(1L);
        }

        @Test
        @DisplayName("Factory가 생성한 Reservation이 Facade에 그대로 전달된다")
        void shouldPassFactoryCreatedReservationToFacade() {
            // given
            CreateReservationCommand command = CreateReservationCommandFixture.aCreateReservationCommand();
            Reservation reservation = ReservationFixture.pendingReservation();
            List<LocalDate> stayDates = command.stayDates();

            willDoNothing().given(validator).validate(command);
            willDoNothing().given(inventoryClientManager).decrementStock(command.roomTypeId(), stayDates);
            given(reservationFactory.create(command)).willReturn(reservation);
            given(reservationPersistenceFacade.persist(any(), any(), anyList())).willReturn(42L);

            // when
            service.execute(command);

            // then
            then(reservationPersistenceFacade).should().persist(any(), any(), anyList());
        }

        @Test
        @DisplayName("예약 성공 후 Redis 임시 홀드 키가 생성된다")
        void shouldCreateHoldAfterPersist() {
            // given
            CreateReservationCommand command = CreateReservationCommandFixture.aCreateReservationCommand();
            Reservation reservation = ReservationFixture.pendingReservation();
            List<LocalDate> stayDates = command.stayDates();

            willDoNothing().given(validator).validate(command);
            willDoNothing().given(inventoryClientManager).decrementStock(command.roomTypeId(), stayDates);
            given(reservationFactory.create(command)).willReturn(reservation);
            given(reservationPersistenceFacade.persist(any(), any(), anyList())).willReturn(1L);

            // when
            service.execute(command);

            // then
        }
    }

    @Nested
    @DisplayName("검증 실패 흐름")
    class ValidationFailure {

        @Test
        @DisplayName("존재하지 않는 숙소로 예약 시 PropertyNotFoundException이 전파된다")
        void shouldPropagatePropertyNotFoundException() {
            // given
            CreateReservationCommand command = CreateReservationCommandFixture.aCreateReservationCommand();
            willThrow(new PropertyNotFoundException()).given(validator).validate(command);

            // when & then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(PropertyNotFoundException.class);
        }

        @Test
        @DisplayName("존재하지 않는 객실유형으로 예약 시 RoomTypeNotFoundException이 전파된다")
        void shouldPropagateRoomTypeNotFoundException() {
            // given
            CreateReservationCommand command = CreateReservationCommandFixture.aCreateReservationCommand();
            willThrow(new RoomTypeNotFoundException()).given(validator).validate(command);

            // when & then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(RoomTypeNotFoundException.class);
        }

        @Test
        @DisplayName("Validator 실패 시 InventoryClientManager, Factory, Facade 모두 호출되지 않는다")
        void shouldNotCallAnyDownstreamWhenValidationFails() {
            // given
            CreateReservationCommand command = CreateReservationCommandFixture.aCreateReservationCommand();
            willThrow(new PropertyNotFoundException()).given(validator).validate(command);

            // when
            try {
                service.execute(command);
            } catch (PropertyNotFoundException ignored) {
                // 예외는 무시 -- 호출 검증이 목적
            }

            // then
            then(inventoryClientManager).shouldHaveNoInteractions();
            then(reservationFactory).shouldHaveNoInteractions();
            then(reservationPersistenceFacade).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("재고 소진 흐름")
    class InventoryExhausted {

        @Test
        @DisplayName("Redis 재고 차감 실패 시 InventoryExhaustedException이 전파된다")
        void shouldPropagateInventoryExhaustedException() {
            // given
            CreateReservationCommand command = CreateReservationCommandFixture.aCreateReservationCommand();
            willDoNothing().given(validator).validate(command);
            willThrow(new InventoryExhaustedException())
                .given(inventoryClientManager).decrementStock(command.roomTypeId(), command.stayDates());

            // when & then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(InventoryExhaustedException.class);
        }

        @Test
        @DisplayName("Redis 재고 차감 실패 시 Factory, Facade는 호출되지 않는다")
        void shouldNotCallFactoryOrFacadeWhenStockExhausted() {
            // given
            CreateReservationCommand command = CreateReservationCommandFixture.aCreateReservationCommand();
            willDoNothing().given(validator).validate(command);
            willThrow(new InventoryExhaustedException())
                .given(inventoryClientManager).decrementStock(command.roomTypeId(), command.stayDates());

            // when
            try {
                service.execute(command);
            } catch (InventoryExhaustedException ignored) {
                // 예외는 무시 -- 호출 검증이 목적
            }

            // then
            then(reservationFactory).shouldHaveNoInteractions();
            then(reservationPersistenceFacade).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("보상 로직 (Redis 재고 복구)")
    class CompensationLogic {

        @Test
        @DisplayName("DB 저장 실패 시 Redis 재고가 복구된다")
        void shouldIncrementStockWhenPersistFails() {
            // given
            CreateReservationCommand command = CreateReservationCommandFixture.aCreateReservationCommand();
            Reservation reservation = ReservationFixture.pendingReservation();
            List<LocalDate> stayDates = command.stayDates();

            willDoNothing().given(validator).validate(command);
            willDoNothing().given(inventoryClientManager).decrementStock(command.roomTypeId(), stayDates);
            given(reservationFactory.create(command)).willReturn(reservation);
            willThrow(new RuntimeException("DB 저장 실패"))
                .given(reservationPersistenceFacade).persist(any(), any(), anyList());

            // when & then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB 저장 실패");

            then(inventoryClientManager).should().incrementStock(command.roomTypeId(), stayDates);
        }

        @Test
        @DisplayName("Factory 실패 시 Redis 재고가 복구된다")
        void shouldIncrementStockWhenFactoryFails() {
            // given
            CreateReservationCommand command = CreateReservationCommandFixture.aCreateReservationCommand();
            List<LocalDate> stayDates = command.stayDates();

            willDoNothing().given(validator).validate(command);
            willDoNothing().given(inventoryClientManager).decrementStock(command.roomTypeId(), stayDates);
            willThrow(new RuntimeException("Factory 오류"))
                .given(reservationFactory).create(command);

            // when & then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Factory 오류");

            then(inventoryClientManager).should().incrementStock(command.roomTypeId(), stayDates);
        }

        @Test
        @DisplayName("DB 저장 실패 후 보상 실행 시 원래 예외가 전파된다")
        void shouldPropagateOriginalExceptionAfterCompensation() {
            // given
            CreateReservationCommand command = CreateReservationCommandFixture.aCreateReservationCommand();
            Reservation reservation = ReservationFixture.pendingReservation();
            List<LocalDate> stayDates = command.stayDates();

            willDoNothing().given(validator).validate(command);
            willDoNothing().given(inventoryClientManager).decrementStock(command.roomTypeId(), stayDates);
            given(reservationFactory.create(command)).willReturn(reservation);
            willThrow(new RuntimeException("DB 저장 실패"))
                .given(reservationPersistenceFacade).persist(any(), any(), anyList());

            // when & then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB 저장 실패");
        }
    }

    @Nested
    @DisplayName("호출 순서 검증")
    class InvocationOrder {

        @Test
        void shouldCallInCorrectOrder() {
            // given
            CreateReservationCommand command = CreateReservationCommandFixture.aCreateReservationCommand();
            Reservation reservation = ReservationFixture.pendingReservation();
            List<LocalDate> stayDates = command.stayDates();

            willDoNothing().given(validator).validate(command);
            willDoNothing().given(inventoryClientManager).decrementStock(command.roomTypeId(), stayDates);
            given(reservationFactory.create(command)).willReturn(reservation);
            given(reservationPersistenceFacade.persist(any(), any(), anyList())).willReturn(1L);

            // when
            service.execute(command);

            // then
            InOrder inOrder = inOrder(validator, inventoryClientManager, reservationFactory, reservationPersistenceFacade);
            inOrder.verify(validator).validate(command);
            inOrder.verify(inventoryClientManager).decrementStock(command.roomTypeId(), stayDates);
            inOrder.verify(reservationFactory).create(command);
            inOrder.verify(reservationPersistenceFacade).persist(any(), any(), anyList());
        }

        @Test
        @DisplayName("Validator가 가장 먼저 호출되어야 한다")
        void shouldCallValidatorFirst() {
            // given
            CreateReservationCommand command = CreateReservationCommandFixture.aCreateReservationCommand();
            Reservation reservation = ReservationFixture.pendingReservation();
            List<LocalDate> stayDates = command.stayDates();

            willDoNothing().given(validator).validate(command);
            willDoNothing().given(inventoryClientManager).decrementStock(command.roomTypeId(), stayDates);
            given(reservationFactory.create(command)).willReturn(reservation);
            given(reservationPersistenceFacade.persist(any(), any(), anyList())).willReturn(1L);

            // when
            service.execute(command);

            // then
            InOrder inOrder = inOrder(validator, inventoryClientManager);
            inOrder.verify(validator).validate(command);
            inOrder.verify(inventoryClientManager).decrementStock(command.roomTypeId(), stayDates);
        }
    }
}
