package com.ryuqq.otatoy.application.roomtype.service;

import com.ryuqq.otatoy.application.roomtype.dto.RoomTypeBundle;
import com.ryuqq.otatoy.application.roomtype.dto.RoomTypeBundleFixture;
import com.ryuqq.otatoy.application.roomtype.dto.command.RegisterRoomTypeCommand;
import com.ryuqq.otatoy.application.roomtype.dto.command.RegisterRoomTypeCommandFixture;
import com.ryuqq.otatoy.application.roomtype.facade.RoomTypePersistenceFacade;
import com.ryuqq.otatoy.application.roomtype.factory.RoomTypeFactory;
import com.ryuqq.otatoy.application.roomtype.validator.RoomTypeRegistrationValidator;
import com.ryuqq.otatoy.domain.property.PropertyNotFoundException;
import com.ryuqq.otatoy.domain.roomtype.InvalidRoomTypeException;

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
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class RegisterRoomTypeServiceTest {

    @Mock
    RoomTypeRegistrationValidator validator;

    @Mock
    RoomTypeFactory roomTypeFactory;

    @Mock
    RoomTypePersistenceFacade roomTypePersistenceFacade;

    @InjectMocks
    RegisterRoomTypeService service;

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("유효한 입력으로 객실 등록 성공 시 RoomTypeId를 반환한다")
        void shouldRegisterRoomTypeAndReturnId() {
            // given
            var command = RegisterRoomTypeCommandFixture.aRegisterRoomTypeCommand();
            var bundle = RoomTypeBundleFixture.aRoomTypeBundle();
            willDoNothing().given(validator).validate(command);
            given(roomTypeFactory.createBundle(command)).willReturn(bundle);
            given(roomTypePersistenceFacade.persist(bundle)).willReturn(100L);

            // when
            Long result = service.execute(command);

            // then
            assertThat(result).isEqualTo(100L);
        }

        @Test
        @DisplayName("Factory가 생성한 번들이 Facade에 그대로 전달된다")
        void shouldPassBundleToFacade() {
            // given
            var command = RegisterRoomTypeCommandFixture.aRegisterRoomTypeCommand();
            var bundle = RoomTypeBundleFixture.aRoomTypeBundle();
            willDoNothing().given(validator).validate(command);
            given(roomTypeFactory.createBundle(command)).willReturn(bundle);
            given(roomTypePersistenceFacade.persist(bundle)).willReturn(1L);

            // when
            service.execute(command);

            // then
            then(roomTypePersistenceFacade).should().persist(bundle);
        }
    }

    @Nested
    @DisplayName("검증 실패 흐름")
    class ValidationFailure {

        @Test
        @DisplayName("존재하지 않는 숙소로 등록 시 PropertyNotFoundException이 전파된다")
        void shouldPropagatePropertyNotFoundException() {
            // given
            var command = RegisterRoomTypeCommandFixture.aRegisterRoomTypeCommand();
            willThrow(new PropertyNotFoundException()).given(validator).validate(command);

            // when & then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(PropertyNotFoundException.class);
        }

        @Test
        @DisplayName("Factory에서 도메인 검증 실패 시 예외가 전파된다")
        void shouldPropagateFactoryException() {
            // given
            var command = RegisterRoomTypeCommandFixture.aRegisterRoomTypeCommand();
            willDoNothing().given(validator).validate(command);
            given(roomTypeFactory.createBundle(command))
                .willThrow(new InvalidRoomTypeException("최대 인원은 기본 인원 이상이어야 합니다"));

            // when & then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(InvalidRoomTypeException.class);
        }

        @Test
        @DisplayName("Validator 실패 시 Factory와 Facade는 호출되지 않는다")
        void shouldNotCallFactoryOrFacadeWhenValidationFails() {
            // given
            var command = RegisterRoomTypeCommandFixture.aRegisterRoomTypeCommand();
            willThrow(new PropertyNotFoundException()).given(validator).validate(command);

            // when
            try { service.execute(command); } catch (PropertyNotFoundException ignored) {}

            // then
            then(roomTypeFactory).should(never()).createBundle(any());
            then(roomTypePersistenceFacade).should(never()).persist(any());
        }

        @Test
        @DisplayName("Factory 실패 시 Facade는 호출되지 않는다")
        void shouldNotCallFacadeWhenFactoryFails() {
            // given
            var command = RegisterRoomTypeCommandFixture.aRegisterRoomTypeCommand();
            willDoNothing().given(validator).validate(command);
            given(roomTypeFactory.createBundle(command))
                .willThrow(new InvalidRoomTypeException("검증 실패"));

            // when
            try { service.execute(command); } catch (InvalidRoomTypeException ignored) {}

            // then
            then(roomTypePersistenceFacade).should(never()).persist(any());
        }
    }

    @Nested
    @DisplayName("호출 순서 검증")
    class InvocationOrder {

        @Test
        @DisplayName("validate() -> createBundle() -> persist() 순서로 호출된다")
        void shouldCallInCorrectOrder() {
            // given
            var command = RegisterRoomTypeCommandFixture.aRegisterRoomTypeCommand();
            var bundle = RoomTypeBundleFixture.aRoomTypeBundle();
            willDoNothing().given(validator).validate(command);
            given(roomTypeFactory.createBundle(command)).willReturn(bundle);
            given(roomTypePersistenceFacade.persist(bundle)).willReturn(1L);

            // when
            service.execute(command);

            // then
            InOrder inOrder = inOrder(validator, roomTypeFactory, roomTypePersistenceFacade);
            inOrder.verify(validator).validate(command);
            inOrder.verify(roomTypeFactory).createBundle(command);
            inOrder.verify(roomTypePersistenceFacade).persist(bundle);
        }
    }

    @Nested
    @DisplayName("Facade 인자 검증")
    class FacadeArgumentVerification {

        @Test
        @DisplayName("침대/전망 없는 번들도 빈 리스트로 Facade에 전달된다")
        void shouldPassEmptyBundle() {
            // given
            var command = RegisterRoomTypeCommandFixture.aRegisterRoomTypeCommand();
            var bundle = RoomTypeBundleFixture.anEmptyRoomTypeBundle();
            willDoNothing().given(validator).validate(command);
            given(roomTypeFactory.createBundle(command)).willReturn(bundle);
            given(roomTypePersistenceFacade.persist(bundle)).willReturn(1L);

            // when
            service.execute(command);

            // then
            then(roomTypePersistenceFacade).should().persist(bundle);
            assertThat(bundle.beds()).isEmpty();
            assertThat(bundle.views()).isEmpty();
        }
    }
}
