package com.ryuqq.otatoy.application.reservation.factory;

import com.ryuqq.otatoy.application.common.factory.TimeProvider;
import com.ryuqq.otatoy.application.reservation.dto.command.ConfirmReservationCommand;
import com.ryuqq.otatoy.application.reservation.dto.command.ConfirmReservationCommandFixture;
import com.ryuqq.otatoy.application.reservation.dto.command.CreateReservationCommand;
import com.ryuqq.otatoy.application.reservation.dto.command.CreateReservationCommandFixture;
import com.ryuqq.otatoy.domain.reservation.Reservation;
import com.ryuqq.otatoy.domain.reservation.ReservationSession;
import com.ryuqq.otatoy.domain.reservation.ReservationSessionFixture;
import com.ryuqq.otatoy.domain.reservation.ReservationStatus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * ReservationFactory 단위 테스트.
 * TimeProvider를 Mock으로 대체하여 시간 제어 및 도메인 객체 생성을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class ReservationFactoryTest {

    @Mock
    TimeProvider timeProvider;

    @InjectMocks
    ReservationFactory factory;

    private static final Instant FIXED_NOW = Instant.parse("2026-04-06T10:00:00Z");
    private static final LocalDate FIXED_TODAY = LocalDate.of(2026, 4, 6);

    @Nested
    @DisplayName("create() - Command 기반 예약 생성")
    class CreateFromCommand {

        @Test
        @DisplayName("Command의 필드가 Reservation에 올바르게 매핑된다")
        void shouldMapCommandFieldsToReservation() {
            // given
            given(timeProvider.now()).willReturn(FIXED_NOW);
            given(timeProvider.today()).willReturn(FIXED_TODAY);
            CreateReservationCommand command = CreateReservationCommandFixture.aCreateReservationCommand();

            // when
            Reservation reservation = factory.create(command);

            // then
            assertThat(reservation.customerId()).isEqualTo(command.customerId());
            assertThat(reservation.guestInfo()).isEqualTo(command.guestInfo());
            assertThat(reservation.stayPeriod().startDate()).isEqualTo(command.checkIn());
            assertThat(reservation.stayPeriod().endDate()).isEqualTo(command.checkOut());
            assertThat(reservation.guestCount()).isEqualTo(command.guestCount());
            assertThat(reservation.totalAmount()).isEqualTo(command.totalAmount());
            assertThat(reservation.bookingSnapshot()).isEqualTo(command.bookingSnapshot());
        }

        @Test
        @DisplayName("새로 생성된 Reservation의 상태는 PENDING이다")
        void shouldCreateWithPendingStatus() {
            // given
            given(timeProvider.now()).willReturn(FIXED_NOW);
            given(timeProvider.today()).willReturn(FIXED_TODAY);
            CreateReservationCommand command = CreateReservationCommandFixture.aCreateReservationCommand();

            // when
            Reservation reservation = factory.create(command);

            // then
            assertThat(reservation.status()).isEqualTo(ReservationStatus.PENDING);
        }

        @Test
        @DisplayName("ReservationNo는 RSV- 접두사를 가진다")
        void shouldGenerateReservationNoWithPrefix() {
            // given
            given(timeProvider.now()).willReturn(FIXED_NOW);
            given(timeProvider.today()).willReturn(FIXED_TODAY);
            CreateReservationCommand command = CreateReservationCommandFixture.aCreateReservationCommand();

            // when
            Reservation reservation = factory.create(command);

            // then
            assertThat(reservation.reservationNo().value()).startsWith("RSV-");
        }

        @Test
        @DisplayName("예약 라인이 올바르게 생성된다")
        void shouldCreateReservationLines() {
            // given
            given(timeProvider.now()).willReturn(FIXED_NOW);
            given(timeProvider.today()).willReturn(FIXED_TODAY);
            CreateReservationCommand command = CreateReservationCommandFixture.aCreateReservationCommand();

            // when
            Reservation reservation = factory.create(command);

            // then
            assertThat(reservation.lines()).hasSize(command.lines().size());
            assertThat(reservation.lines().get(0).ratePlanId())
                .isEqualTo(command.lines().get(0).ratePlanId());
            assertThat(reservation.lines().get(0).roomCount())
                .isEqualTo(command.lines().get(0).roomCount());
        }

        @Test
        @DisplayName("새로 생성된 Reservation의 id는 null이다")
        void shouldCreateWithNullId() {
            // given
            given(timeProvider.now()).willReturn(FIXED_NOW);
            given(timeProvider.today()).willReturn(FIXED_TODAY);
            CreateReservationCommand command = CreateReservationCommandFixture.aCreateReservationCommand();

            // when
            Reservation reservation = factory.create(command);

            // then
            assertThat(reservation.id()).isNull();
        }
    }

    @Nested
    @DisplayName("createFromSession() - 세션 기반 예약 생성")
    class CreateFromSession {

        @Test
        @DisplayName("세션과 Command의 필드가 Reservation에 올바르게 매핑된다")
        void shouldMapSessionAndCommandFieldsToReservation() {
            // given
            given(timeProvider.now()).willReturn(FIXED_NOW);
            given(timeProvider.today()).willReturn(FIXED_TODAY);
            ReservationSession session = ReservationSessionFixture.reconstitutedPendingSession(1L);
            ConfirmReservationCommand command = ConfirmReservationCommandFixture.aConfirmReservationCommand();

            // when
            Reservation reservation = factory.createFromSession(session, command);

            // then
            assertThat(reservation.customerId()).isEqualTo(command.customerId());
            assertThat(reservation.guestInfo()).isEqualTo(command.guestInfo());
            assertThat(reservation.stayPeriod().startDate()).isEqualTo(session.checkIn());
            assertThat(reservation.stayPeriod().endDate()).isEqualTo(session.checkOut());
            assertThat(reservation.guestCount()).isEqualTo(session.guestCount());
            assertThat(reservation.totalAmount()).isEqualTo(session.totalAmount());
            assertThat(reservation.bookingSnapshot()).isEqualTo(command.bookingSnapshot());
        }

        @Test
        @DisplayName("세션 기반 생성 시에도 RSV- 접두사 ReservationNo가 생성된다")
        void shouldGenerateReservationNoWithPrefixFromSession() {
            // given
            given(timeProvider.now()).willReturn(FIXED_NOW);
            given(timeProvider.today()).willReturn(FIXED_TODAY);
            ReservationSession session = ReservationSessionFixture.reconstitutedPendingSession(1L);
            ConfirmReservationCommand command = ConfirmReservationCommandFixture.aConfirmReservationCommand();

            // when
            Reservation reservation = factory.createFromSession(session, command);

            // then
            assertThat(reservation.reservationNo().value()).startsWith("RSV-");
        }

        @Test
        @DisplayName("세션 기반 생성 시에도 상태는 PENDING이다")
        void shouldCreateWithPendingStatusFromSession() {
            // given
            given(timeProvider.now()).willReturn(FIXED_NOW);
            given(timeProvider.today()).willReturn(FIXED_TODAY);
            ReservationSession session = ReservationSessionFixture.reconstitutedPendingSession(1L);
            ConfirmReservationCommand command = ConfirmReservationCommandFixture.aConfirmReservationCommand();

            // when
            Reservation reservation = factory.createFromSession(session, command);

            // then
            assertThat(reservation.status()).isEqualTo(ReservationStatus.PENDING);
        }
    }
}
