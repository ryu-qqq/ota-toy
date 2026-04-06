package com.ryuqq.otatoy.application.reservation.validator;

import com.ryuqq.otatoy.application.property.manager.PropertyReadManager;
import com.ryuqq.otatoy.application.reservation.dto.command.CreateReservationSessionCommand;
import com.ryuqq.otatoy.application.reservation.dto.command.CreateReservationSessionCommandFixture;
import com.ryuqq.otatoy.application.roomtype.manager.RoomTypeReadManager;
import com.ryuqq.otatoy.domain.property.PropertyNotFoundException;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeNotFoundException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

/**
 * ReservationSessionValidator 단위 테스트.
 * PropertyReadManager, RoomTypeReadManager를 Mock으로 대체하여 검증 로직을 확인한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class ReservationSessionValidatorTest {

    @Mock
    PropertyReadManager propertyReadManager;

    @Mock
    RoomTypeReadManager roomTypeReadManager;

    @InjectMocks
    ReservationSessionValidator validator;

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("숙소와 객실유형이 모두 존재하면 예외 없이 통과한다")
        void shouldPassWhenBothExist() {
            // given
            CreateReservationSessionCommand command = CreateReservationSessionCommandFixture.aCreateReservationSessionCommand();
            willDoNothing().given(propertyReadManager).verifyExists(command.propertyId());
            willDoNothing().given(roomTypeReadManager).verifyExists(command.roomTypeId());

            // when & then
            assertThatCode(() -> validator.validate(command))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("검증 실패 흐름")
    class Failure {

        @Test
        @DisplayName("숙소가 존재하지 않으면 PropertyNotFoundException을 던진다")
        void shouldThrowWhenPropertyNotFound() {
            // given
            CreateReservationSessionCommand command = CreateReservationSessionCommandFixture.aCreateReservationSessionCommand();
            willThrow(new PropertyNotFoundException())
                .given(propertyReadManager).verifyExists(command.propertyId());

            // when & then
            assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(PropertyNotFoundException.class);
        }

        @Test
        @DisplayName("객실유형이 존재하지 않으면 RoomTypeNotFoundException을 던진다")
        void shouldThrowWhenRoomTypeNotFound() {
            // given
            CreateReservationSessionCommand command = CreateReservationSessionCommandFixture.aCreateReservationSessionCommand();
            willDoNothing().given(propertyReadManager).verifyExists(command.propertyId());
            willThrow(new RoomTypeNotFoundException())
                .given(roomTypeReadManager).verifyExists(command.roomTypeId());

            // when & then
            assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(RoomTypeNotFoundException.class);
        }

        @Test
        @DisplayName("숙소 검증 실패 시 객실유형 검증은 호출되지 않는다")
        void shouldNotCheckRoomTypeWhenPropertyFails() {
            // given
            CreateReservationSessionCommand command = CreateReservationSessionCommandFixture.aCreateReservationSessionCommand();
            willThrow(new PropertyNotFoundException())
                .given(propertyReadManager).verifyExists(command.propertyId());

            // when
            try {
                validator.validate(command);
            } catch (PropertyNotFoundException ignored) {
                // 예외는 무시 -- 호출 검증이 목적
            }

            // then
            then(roomTypeReadManager).should(never()).verifyExists(command.roomTypeId());
        }
    }
}
