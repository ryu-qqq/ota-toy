package com.ryuqq.otatoy.application.roomtype.validator;

import com.ryuqq.otatoy.application.property.manager.PropertyReadManager;
import com.ryuqq.otatoy.application.roomtype.dto.command.RegisterRoomTypeCommand;
import com.ryuqq.otatoy.application.roomtype.dto.command.RegisterRoomTypeCommandFixture;
import com.ryuqq.otatoy.domain.property.PropertyNotFoundException;

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
 * RoomTypeRegistrationValidator 단위 테스트.
 * PropertyReadManager를 Mock으로 대체하여 숙소 존재 검증 로직을 확인한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class RoomTypeRegistrationValidatorTest {

    @Mock
    PropertyReadManager propertyReadManager;

    @InjectMocks
    RoomTypeRegistrationValidator validator;

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("숙소가 존재하면 예외 없이 통과한다")
        void shouldPassWhenPropertyExists() {
            // given
            RegisterRoomTypeCommand command = RegisterRoomTypeCommandFixture.aRegisterRoomTypeCommand();
            willDoNothing().given(propertyReadManager).verifyExists(command.propertyId());

            // when & then
            assertThatCode(() -> validator.validate(command))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("PropertyReadManager.verifyExists가 정확한 propertyId로 호출된다")
        void shouldCallVerifyExistsWithCorrectPropertyId() {
            // given
            RegisterRoomTypeCommand command = RegisterRoomTypeCommandFixture.aRegisterRoomTypeCommand();
            willDoNothing().given(propertyReadManager).verifyExists(command.propertyId());

            // when
            validator.validate(command);

            // then
            then(propertyReadManager).should().verifyExists(command.propertyId());
        }
    }

    @Nested
    @DisplayName("검증 실패 흐름")
    class Failure {

        @Test
        @DisplayName("숙소가 존재하지 않으면 PropertyNotFoundException을 던진다")
        void shouldThrowWhenPropertyNotFound() {
            // given
            RegisterRoomTypeCommand command = RegisterRoomTypeCommandFixture.aRegisterRoomTypeCommand();
            willThrow(new PropertyNotFoundException())
                .given(propertyReadManager).verifyExists(command.propertyId());

            // when & then
            assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(PropertyNotFoundException.class);
        }
    }
}
