package com.ryuqq.otatoy.application.reservation.factory;

import com.ryuqq.otatoy.application.common.factory.TimeProvider;
import com.ryuqq.otatoy.application.reservation.dto.command.CreateReservationSessionCommand;
import com.ryuqq.otatoy.application.reservation.dto.command.CreateReservationSessionCommandFixture;
import com.ryuqq.otatoy.domain.reservation.ReservationSession;
import com.ryuqq.otatoy.domain.reservation.ReservationSessionStatus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * ReservationSessionFactory 단위 테스트.
 * TimeProvider를 Mock으로 대체하여 시간 제어 및 도메인 객체 생성을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class ReservationSessionFactoryTest {

    @Mock
    TimeProvider timeProvider;

    @InjectMocks
    ReservationSessionFactory factory;

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("Command의 필드가 ReservationSession에 올바르게 매핑된다")
        void shouldMapCommandFieldsToSession() {
            // given
            Instant fixedNow = Instant.parse("2026-04-06T10:00:00Z");
            given(timeProvider.now()).willReturn(fixedNow);
            CreateReservationSessionCommand command = CreateReservationSessionCommandFixture.aCreateReservationSessionCommand();

            // when
            ReservationSession session = factory.create(command);

            // then
            assertThat(session.propertyId()).isEqualTo(command.propertyId());
            assertThat(session.roomTypeId()).isEqualTo(command.roomTypeId());
            assertThat(session.ratePlanId()).isEqualTo(command.ratePlanId());
            assertThat(session.checkIn()).isEqualTo(command.checkIn());
            assertThat(session.checkOut()).isEqualTo(command.checkOut());
            assertThat(session.guestCount()).isEqualTo(command.guestCount());
            assertThat(session.totalAmount()).isEqualTo(command.totalAmount());
        }

        @Test
        @DisplayName("새로 생성된 세션의 상태는 PENDING이다")
        void shouldCreateSessionWithPendingStatus() {
            // given
            Instant fixedNow = Instant.parse("2026-04-06T10:00:00Z");
            given(timeProvider.now()).willReturn(fixedNow);
            CreateReservationSessionCommand command = CreateReservationSessionCommandFixture.aCreateReservationSessionCommand();

            // when
            ReservationSession session = factory.create(command);

            // then
            assertThat(session.status()).isEqualTo(ReservationSessionStatus.PENDING);
        }

        @Test
        @DisplayName("새로 생성된 세션의 id는 null이다")
        void shouldCreateSessionWithNullId() {
            // given
            Instant fixedNow = Instant.parse("2026-04-06T10:00:00Z");
            given(timeProvider.now()).willReturn(fixedNow);
            CreateReservationSessionCommand command = CreateReservationSessionCommandFixture.aCreateReservationSessionCommand();

            // when
            ReservationSession session = factory.create(command);

            // then
            assertThat(session.id()).isNull();
        }

        @Test
        @DisplayName("TimeProvider에서 제공한 시간이 createdAt에 설정된다")
        void shouldUseTimeProviderForTimestamp() {
            // given
            Instant fixedNow = Instant.parse("2026-04-06T10:00:00Z");
            given(timeProvider.now()).willReturn(fixedNow);
            CreateReservationSessionCommand command = CreateReservationSessionCommandFixture.aCreateReservationSessionCommand();

            // when
            ReservationSession session = factory.create(command);

            // then
            assertThat(session.createdAt()).isEqualTo(fixedNow);
        }

        @Test
        @DisplayName("멱등키가 올바르게 설정된다")
        void shouldSetIdempotencyKey() {
            // given
            Instant fixedNow = Instant.parse("2026-04-06T10:00:00Z");
            given(timeProvider.now()).willReturn(fixedNow);
            CreateReservationSessionCommand command = CreateReservationSessionCommandFixture.aCreateReservationSessionCommand();

            // when
            ReservationSession session = factory.create(command);

            // then
            assertThat(session.idempotencyKey()).isEqualTo(command.idempotencyKey());
        }
    }
}
