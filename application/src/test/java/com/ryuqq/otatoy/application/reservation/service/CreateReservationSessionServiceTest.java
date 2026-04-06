package com.ryuqq.otatoy.application.reservation.service;

import com.ryuqq.otatoy.application.inventory.manager.InventoryClientManager;
import com.ryuqq.otatoy.application.reservation.dto.command.CreateReservationSessionCommand;
import com.ryuqq.otatoy.application.reservation.dto.command.CreateReservationSessionCommandFixture;
import com.ryuqq.otatoy.application.reservation.dto.result.ReservationSessionResult;
import com.ryuqq.otatoy.application.reservation.factory.ReservationSessionFactory;
import com.ryuqq.otatoy.application.reservation.manager.ReservationSessionCommandManager;
import com.ryuqq.otatoy.application.reservation.manager.ReservationSessionReadManager;
import com.ryuqq.otatoy.application.reservation.validator.ReservationSessionValidator;
import com.ryuqq.otatoy.domain.property.PropertyNotFoundException;
import com.ryuqq.otatoy.domain.reservation.ReservationSession;
import com.ryuqq.otatoy.domain.reservation.ReservationSessionFixture;
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
import java.util.Optional;

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
 * CreateReservationSessionService 단위 테스트.
 * Validator, Factory, InventoryClientManager, SessionCommandManager, SessionReadManager를
 * Mock으로 대체하여 Service의 오케스트레이션 로직을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class CreateReservationSessionServiceTest {

    @Mock
    ReservationSessionValidator validator;

    @Mock
    ReservationSessionFactory sessionFactory;

    @Mock
    InventoryClientManager inventoryClientManager;

    @Mock
    ReservationSessionCommandManager sessionCommandManager;

    @Mock
    ReservationSessionReadManager sessionReadManager;

    @InjectMocks
    CreateReservationSessionService service;

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("유효한 입력으로 예약 세션 생성 성공 시 ReservationSessionResult를 반환한다")
        void shouldCreateSessionAndReturnResult() {
            // given
            CreateReservationSessionCommand command = CreateReservationSessionCommandFixture.aCreateReservationSessionCommand();
            ReservationSession session = ReservationSessionFixture.pendingSession();

            given(sessionReadManager.findByIdempotencyKey(command.idempotencyKey()))
                .willReturn(Optional.empty());
            willDoNothing().given(validator).validate(command);
            willDoNothing().given(inventoryClientManager).decrementStock(any(), anyList());
            given(sessionFactory.create(command)).willReturn(session);
            given(sessionCommandManager.persist(session)).willReturn(1L);

            // when
            ReservationSessionResult result = service.execute(command);

            // then
            assertThat(result.sessionId()).isEqualTo(1L);
            assertThat(result.totalAmount()).isEqualTo(session.totalAmount());
            assertThat(result.checkIn()).isEqualTo(session.checkIn());
            assertThat(result.checkOut()).isEqualTo(session.checkOut());
            assertThat(result.guestCount()).isEqualTo(session.guestCount());
        }

        @Test
        @DisplayName("멱등키로 이미 존재하는 세션이 있으면 기존 세션 결과를 반환한다")
        void shouldReturnExistingSessionWhenIdempotencyKeyExists() {
            // given
            CreateReservationSessionCommand command = CreateReservationSessionCommandFixture.aCreateReservationSessionCommand();
            ReservationSession existingSession = ReservationSessionFixture.reconstitutedPendingSession(99L);

            given(sessionReadManager.findByIdempotencyKey(command.idempotencyKey()))
                .willReturn(Optional.of(existingSession));

            // when
            ReservationSessionResult result = service.execute(command);

            // then
            assertThat(result.sessionId()).isEqualTo(99L);
            then(validator).shouldHaveNoInteractions();
            then(inventoryClientManager).shouldHaveNoInteractions();
            then(sessionFactory).shouldHaveNoInteractions();
            then(sessionCommandManager).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("검증 실패 흐름")
    class ValidationFailure {

        @Test
        @DisplayName("숙소가 존재하지 않으면 PropertyNotFoundException이 전파된다")
        void shouldPropagatePropertyNotFoundException() {
            // given
            CreateReservationSessionCommand command = CreateReservationSessionCommandFixture.aCreateReservationSessionCommand();
            given(sessionReadManager.findByIdempotencyKey(command.idempotencyKey()))
                .willReturn(Optional.empty());
            willThrow(new PropertyNotFoundException()).given(validator).validate(command);

            // when & then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(PropertyNotFoundException.class);
        }

        @Test
        @DisplayName("객실유형이 존재하지 않으면 RoomTypeNotFoundException이 전파된다")
        void shouldPropagateRoomTypeNotFoundException() {
            // given
            CreateReservationSessionCommand command = CreateReservationSessionCommandFixture.aCreateReservationSessionCommand();
            given(sessionReadManager.findByIdempotencyKey(command.idempotencyKey()))
                .willReturn(Optional.empty());
            willThrow(new RoomTypeNotFoundException()).given(validator).validate(command);

            // when & then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(RoomTypeNotFoundException.class);
        }

        @Test
        @DisplayName("Validator 실패 시 재고 차감, Factory, CommandManager는 호출되지 않는다")
        void shouldNotCallDownstreamWhenValidationFails() {
            // given
            CreateReservationSessionCommand command = CreateReservationSessionCommandFixture.aCreateReservationSessionCommand();
            given(sessionReadManager.findByIdempotencyKey(command.idempotencyKey()))
                .willReturn(Optional.empty());
            willThrow(new PropertyNotFoundException()).given(validator).validate(command);

            // when
            try {
                service.execute(command);
            } catch (PropertyNotFoundException ignored) {
                // 예외는 무시 -- 호출 검증이 목적
            }

            // then
            then(inventoryClientManager).should(never()).decrementStock(any(), anyList());
            then(sessionFactory).should(never()).create(any());
            then(sessionCommandManager).should(never()).persist(any());
        }
    }

    @Nested
    @DisplayName("호출 순서 검증")
    class InvocationOrder {

        @Test
        @DisplayName("멱등키 확인 -> validate -> decrementStock -> create -> persist 순서로 호출된다")
        void shouldCallInCorrectOrder() {
            // given
            CreateReservationSessionCommand command = CreateReservationSessionCommandFixture.aCreateReservationSessionCommand();
            ReservationSession session = ReservationSessionFixture.pendingSession();

            given(sessionReadManager.findByIdempotencyKey(command.idempotencyKey()))
                .willReturn(Optional.empty());
            willDoNothing().given(validator).validate(command);
            willDoNothing().given(inventoryClientManager).decrementStock(any(), anyList());
            given(sessionFactory.create(command)).willReturn(session);
            given(sessionCommandManager.persist(session)).willReturn(1L);

            // when
            service.execute(command);

            // then
            InOrder inOrder = inOrder(sessionReadManager, validator, inventoryClientManager, sessionFactory, sessionCommandManager);
            inOrder.verify(sessionReadManager).findByIdempotencyKey(command.idempotencyKey());
            inOrder.verify(validator).validate(command);
            inOrder.verify(inventoryClientManager).decrementStock(command.roomTypeId(), command.stayDates());
            inOrder.verify(sessionFactory).create(command);
            inOrder.verify(sessionCommandManager).persist(session);
        }
    }

    @Nested
    @DisplayName("재고 보상 (트랜잭션 경계 검증)")
    class InventoryCompensation {

        @Test
        @DisplayName("Factory 실패 시 Redis 재고를 복구한다")
        void shouldRestoreStockWhenFactoryFails() {
            // given
            CreateReservationSessionCommand command = CreateReservationSessionCommandFixture.aCreateReservationSessionCommand();
            List<LocalDate> stayDates = command.stayDates();

            given(sessionReadManager.findByIdempotencyKey(command.idempotencyKey()))
                .willReturn(Optional.empty());
            willDoNothing().given(validator).validate(command);
            willDoNothing().given(inventoryClientManager).decrementStock(any(), anyList());
            given(sessionFactory.create(command)).willThrow(new RuntimeException("Factory 오류"));

            // when & then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(RuntimeException.class);

            then(inventoryClientManager).should().incrementStock(command.roomTypeId(), stayDates);
        }

        @Test
        @DisplayName("DB 저장 실패 시 Redis 재고를 복구한다")
        void shouldRestoreStockWhenPersistFails() {
            // given
            CreateReservationSessionCommand command = CreateReservationSessionCommandFixture.aCreateReservationSessionCommand();
            ReservationSession session = ReservationSessionFixture.pendingSession();
            List<LocalDate> stayDates = command.stayDates();

            given(sessionReadManager.findByIdempotencyKey(command.idempotencyKey()))
                .willReturn(Optional.empty());
            willDoNothing().given(validator).validate(command);
            willDoNothing().given(inventoryClientManager).decrementStock(any(), anyList());
            given(sessionFactory.create(command)).willReturn(session);
            given(sessionCommandManager.persist(session)).willThrow(new RuntimeException("DB 오류"));

            // when & then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(RuntimeException.class);

            then(inventoryClientManager).should().incrementStock(command.roomTypeId(), stayDates);
        }

        @Test
        @DisplayName("정상 흐름에서는 재고 복구가 호출되지 않는다")
        void shouldNotRestoreStockOnSuccess() {
            // given
            CreateReservationSessionCommand command = CreateReservationSessionCommandFixture.aCreateReservationSessionCommand();
            ReservationSession session = ReservationSessionFixture.pendingSession();

            given(sessionReadManager.findByIdempotencyKey(command.idempotencyKey()))
                .willReturn(Optional.empty());
            willDoNothing().given(validator).validate(command);
            willDoNothing().given(inventoryClientManager).decrementStock(any(), anyList());
            given(sessionFactory.create(command)).willReturn(session);
            given(sessionCommandManager.persist(session)).willReturn(1L);

            // when
            service.execute(command);

            // then
            then(inventoryClientManager).should(never()).incrementStock(any(), anyList());
        }
    }
}
