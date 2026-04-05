package com.ryuqq.otatoy.application.property.service;

import com.ryuqq.otatoy.application.property.dto.command.RegisterPropertyCommand;
import com.ryuqq.otatoy.application.property.dto.command.RegisterPropertyCommandFixture;
import com.ryuqq.otatoy.application.property.factory.PropertyFactory;
import com.ryuqq.otatoy.application.property.manager.PropertyCommandManager;
import com.ryuqq.otatoy.application.property.validator.PropertyRegistrationValidator;
import com.ryuqq.otatoy.domain.partner.PartnerNotFoundException;
import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertyFixture;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeNotFoundException;

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

/**
 * RegisterPropertyService 단위 테스트.
 * Validator, Factory, CommandManager를 Mock으로 대체하여
 * Service의 오케스트레이션 로직을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@ExtendWith(MockitoExtension.class)
class RegisterPropertyServiceTest {

    @Mock
    PropertyRegistrationValidator validator;

    @Mock
    PropertyFactory propertyFactory;

    @Mock
    PropertyCommandManager propertyCommandManager;

    @InjectMocks
    RegisterPropertyService service;

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("유효한 입력으로 숙소 등록 성공 시 PropertyId를 반환한다")
        void shouldRegisterPropertyAndReturnId() {
            // given
            RegisterPropertyCommand command = RegisterPropertyCommandFixture.aRegisterPropertyCommand();
            Property property = PropertyFixture.aProperty();

            willDoNothing().given(validator).validate(command);
            given(propertyFactory.createProperty(command)).willReturn(property);
            given(propertyCommandManager.persist(property)).willReturn(1L);

            // when
            Long result = service.execute(command);

            // then
            assertThat(result).isEqualTo(1L);
        }

        @Test
        @DisplayName("Factory가 생성한 Property가 CommandManager에 그대로 전달된다")
        void shouldPassFactoryCreatedPropertyToCommandManager() {
            // given
            RegisterPropertyCommand command = RegisterPropertyCommandFixture.aRegisterPropertyCommand();
            Property property = PropertyFixture.aProperty();

            willDoNothing().given(validator).validate(command);
            given(propertyFactory.createProperty(command)).willReturn(property);
            given(propertyCommandManager.persist(property)).willReturn(42L);

            // when
            service.execute(command);

            // then
            then(propertyCommandManager).should().persist(property);
        }
    }

    @Nested
    @DisplayName("검증 실패 흐름")
    class ValidationFailure {

        @Test
        @DisplayName("존재하지 않는 파트너로 등록 시 PartnerNotFoundException이 전파된다")
        void shouldPropagatePartnerNotFoundException() {
            // given
            RegisterPropertyCommand command = RegisterPropertyCommandFixture.aRegisterPropertyCommand();
            willThrow(new PartnerNotFoundException()).given(validator).validate(command);

            // when & then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(PartnerNotFoundException.class);
        }

        @Test
        @DisplayName("존재하지 않는 숙소유형으로 등록 시 PropertyTypeNotFoundException이 전파된다")
        void shouldPropagatePropertyTypeNotFoundException() {
            // given
            RegisterPropertyCommand command = RegisterPropertyCommandFixture.aRegisterPropertyCommand();
            willThrow(new PropertyTypeNotFoundException()).given(validator).validate(command);

            // when & then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(PropertyTypeNotFoundException.class);
        }

        @Test
        @DisplayName("Validator 실패 시 Factory와 CommandManager는 호출되지 않는다")
        void shouldNotCallFactoryOrManagerWhenValidationFails() {
            // given
            RegisterPropertyCommand command = RegisterPropertyCommandFixture.aRegisterPropertyCommand();
            willThrow(new PartnerNotFoundException()).given(validator).validate(command);

            // when
            try {
                service.execute(command);
            } catch (PartnerNotFoundException ignored) {
                // 예외는 무시 -- 호출 검증이 목적
            }

            // then
            then(propertyFactory).should(never()).createProperty(any());
            then(propertyCommandManager).should(never()).persist(any());
        }
    }

    @Nested
    @DisplayName("호출 순서 검증")
    class InvocationOrder {

        @Test
        @DisplayName("validate() -> createProperty() -> persist() 순서로 호출된다")
        void shouldCallInCorrectOrder() {
            // given
            RegisterPropertyCommand command = RegisterPropertyCommandFixture.aRegisterPropertyCommand();
            Property property = PropertyFixture.aProperty();

            willDoNothing().given(validator).validate(command);
            given(propertyFactory.createProperty(command)).willReturn(property);
            given(propertyCommandManager.persist(property)).willReturn(1L);

            // when
            service.execute(command);

            // then
            InOrder inOrder = inOrder(validator, propertyFactory, propertyCommandManager);
            inOrder.verify(validator).validate(command);
            inOrder.verify(propertyFactory).createProperty(command);
            inOrder.verify(propertyCommandManager).persist(property);
        }

        @Test
        @DisplayName("Validator가 가장 먼저 호출되어야 한다")
        void shouldCallValidatorFirst() {
            // given
            RegisterPropertyCommand command = RegisterPropertyCommandFixture.aRegisterPropertyCommand();
            Property property = PropertyFixture.aProperty();

            willDoNothing().given(validator).validate(command);
            given(propertyFactory.createProperty(command)).willReturn(property);
            given(propertyCommandManager.persist(property)).willReturn(1L);

            // when
            service.execute(command);

            // then
            InOrder inOrder = inOrder(validator, propertyFactory);
            inOrder.verify(validator).validate(command);
            inOrder.verify(propertyFactory).createProperty(command);
        }
    }
}
