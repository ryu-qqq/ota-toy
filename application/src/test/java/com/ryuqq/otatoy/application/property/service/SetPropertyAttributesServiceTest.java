package com.ryuqq.otatoy.application.property.service;

import com.ryuqq.otatoy.application.property.dto.command.SetPropertyAttributesCommand;
import com.ryuqq.otatoy.application.property.dto.command.SetPropertyAttributesCommandFixture;
import com.ryuqq.otatoy.application.property.factory.PropertyAttributeValueFactory;
import com.ryuqq.otatoy.application.property.manager.PropertyAttributeValueCommandManager;
import com.ryuqq.otatoy.application.property.manager.PropertyAttributeValueReadManager;
import com.ryuqq.otatoy.application.property.validator.PropertyAttributesValidator;
import com.ryuqq.otatoy.domain.property.PropertyAttributeValues;
import com.ryuqq.otatoy.domain.property.PropertyNotFoundException;
import com.ryuqq.otatoy.domain.property.RequiredPropertyAttributeMissingException;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SetPropertyAttributesServiceTest {

    @Mock PropertyAttributesValidator validator;
    @Mock PropertyAttributeValueFactory propertyAttributeValueFactory;
    @Mock PropertyAttributeValueReadManager propertyAttributeValueReadManager;
    @Mock PropertyAttributeValueCommandManager propertyAttributeValueCommandManager;
    @InjectMocks SetPropertyAttributesService service;

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("빈 목록끼리 비교하면 변경 없으므로 persistAll 호출 안 됨")
        void shouldNotPersistWhenNoChanges() {
            var command = SetPropertyAttributesCommandFixture.aSetPropertyAttributesCommand();
            var existing = PropertyAttributeValues.reconstitute(List.of());
            var newValues = PropertyAttributeValues.forNew(List.of());

            willDoNothing().given(validator).validate(command);
            given(propertyAttributeValueReadManager.getByPropertyId(command.propertyId())).willReturn(existing);
            given(propertyAttributeValueFactory.create(command)).willReturn(newValues);

            service.execute(command);

            then(propertyAttributeValueCommandManager).should(never()).persistAll(anyList());
        }
    }

    @Nested
    @DisplayName("검증 실패 흐름")
    class ValidationFailure {

        @Test
        @DisplayName("PropertyNotFoundException 전파")
        void shouldPropagatePropertyNotFoundException() {
            var command = SetPropertyAttributesCommandFixture.aSetPropertyAttributesCommand();
            willThrow(new PropertyNotFoundException()).given(validator).validate(command);

            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(PropertyNotFoundException.class);
        }

        @Test
        @DisplayName("RequiredPropertyAttributeMissingException 전파")
        void shouldPropagateRequiredAttributeMissingException() {
            var command = SetPropertyAttributesCommandFixture.aSetPropertyAttributesCommand();
            willThrow(new RequiredPropertyAttributeMissingException(Set.of()))
                .given(validator).validate(command);

            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(RequiredPropertyAttributeMissingException.class);
        }

        @Test
        @DisplayName("Validator 실패 시 후속 호출 없음")
        void shouldNotCallOtherManagersWhenValidationFails() {
            var command = SetPropertyAttributesCommandFixture.aSetPropertyAttributesCommand();
            willThrow(new PropertyNotFoundException()).given(validator).validate(command);

            try { service.execute(command); } catch (PropertyNotFoundException ignored) {}

            then(propertyAttributeValueReadManager).should(never()).getByPropertyId(any());
            then(propertyAttributeValueFactory).should(never()).create(any());
            then(propertyAttributeValueCommandManager).should(never()).persistAll(anyList());
        }
    }

    @Nested
    @DisplayName("호출 순서 검증")
    class InvocationOrder {

        @Test
        @DisplayName("validate -> getByPropertyId -> create 순서로 호출된다")
        void shouldCallInCorrectOrder() {
            var command = SetPropertyAttributesCommandFixture.aSetPropertyAttributesCommand();
            var existing = PropertyAttributeValues.reconstitute(List.of());
            var newValues = PropertyAttributeValues.forNew(List.of());

            willDoNothing().given(validator).validate(command);
            given(propertyAttributeValueReadManager.getByPropertyId(command.propertyId())).willReturn(existing);
            given(propertyAttributeValueFactory.create(command)).willReturn(newValues);

            service.execute(command);

            InOrder inOrder = inOrder(validator, propertyAttributeValueReadManager, propertyAttributeValueFactory);
            inOrder.verify(validator).validate(command);
            inOrder.verify(propertyAttributeValueReadManager).getByPropertyId(command.propertyId());
            inOrder.verify(propertyAttributeValueFactory).create(command);
        }
    }
}
