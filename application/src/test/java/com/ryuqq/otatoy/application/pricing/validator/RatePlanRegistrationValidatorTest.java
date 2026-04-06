package com.ryuqq.otatoy.application.pricing.validator;

import com.ryuqq.otatoy.application.pricing.dto.command.RegisterRatePlanCommand;
import com.ryuqq.otatoy.application.pricing.dto.command.RegisterRatePlanCommandFixture;
import com.ryuqq.otatoy.application.roomtype.manager.RoomTypeReadManager;
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

/**
 * RatePlanRegistrationValidator 단위 테스트.
 * RoomTypeReadManager.verifyExists() 위임 및 예외 전파를 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class RatePlanRegistrationValidatorTest {

    @Mock RoomTypeReadManager roomTypeReadManager;
    @InjectMocks RatePlanRegistrationValidator validator;

    @Nested
    @DisplayName("검증 성공")
    class ValidationSuccess {

        @Test
        @DisplayName("존재하는 RoomTypeId로 검증 시 예외가 발생하지 않는다")
        void shouldPassWhenRoomTypeExists() {
            // given
            RegisterRatePlanCommand command = RegisterRatePlanCommandFixture.aRegisterRatePlanCommand();
            willDoNothing().given(roomTypeReadManager).verifyExists(command.roomTypeId());

            // when & then
            assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("RoomTypeReadManager.verifyExists()에 Command의 roomTypeId가 전달된다")
        void shouldDelegateToRoomTypeReadManager() {
            // given
            RegisterRatePlanCommand command = RegisterRatePlanCommandFixture.aRegisterRatePlanCommand();
            willDoNothing().given(roomTypeReadManager).verifyExists(command.roomTypeId());

            // when
            validator.validate(command);

            // then
            then(roomTypeReadManager).should().verifyExists(command.roomTypeId());
        }
    }

    @Nested
    @DisplayName("검증 실패")
    class ValidationFailure {

        @Test
        @DisplayName("존재하지 않는 RoomTypeId로 검증 시 RoomTypeNotFoundException이 전파된다")
        void shouldThrowWhenRoomTypeNotFound() {
            // given
            RegisterRatePlanCommand command = RegisterRatePlanCommandFixture.aRegisterRatePlanCommand();
            willThrow(new RoomTypeNotFoundException())
                .given(roomTypeReadManager).verifyExists(command.roomTypeId());

            // when & then
            assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(RoomTypeNotFoundException.class);
        }
    }
}
