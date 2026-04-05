package com.ryuqq.otatoy.application.property.service;

import com.ryuqq.otatoy.application.property.dto.command.SetPropertyAmenitiesCommand;
import com.ryuqq.otatoy.application.property.dto.command.SetPropertyAmenitiesCommandFixture;
import com.ryuqq.otatoy.application.property.factory.PropertyAmenityFactory;
import com.ryuqq.otatoy.application.property.manager.PropertyAmenityCommandManager;
import com.ryuqq.otatoy.application.property.manager.PropertyAmenityReadManager;
import com.ryuqq.otatoy.application.property.validator.PropertyAmenitiesValidator;
import com.ryuqq.otatoy.domain.property.PropertyAmenities;
import com.ryuqq.otatoy.domain.property.PropertyNotFoundException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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
class SetPropertyAmenitiesServiceTest {

    @Mock PropertyAmenitiesValidator validator;
    @Mock PropertyAmenityFactory propertyAmenityFactory;
    @Mock PropertyAmenityReadManager propertyAmenityReadManager;
    @Mock PropertyAmenityCommandManager propertyAmenityCommandManager;
    @InjectMocks SetPropertyAmenitiesService service;

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("빈 목록끼리 비교하면 변경 없으므로 persistAll 호출 안 됨")
        void shouldNotPersistWhenNoChanges() {
            // given
            var command = SetPropertyAmenitiesCommandFixture.aSetPropertyAmenitiesCommand();
            var existing = PropertyAmenities.reconstitute(List.of());
            var newAmenities = PropertyAmenities.forNew(List.of());

            willDoNothing().given(validator).validate(command.propertyId());
            given(propertyAmenityReadManager.getByPropertyId(command.propertyId())).willReturn(existing);
            given(propertyAmenityFactory.createAmenities(command)).willReturn(newAmenities);

            // when
            service.execute(command);

            // then
            then(propertyAmenityCommandManager).should(never()).persistAll(anyList());
        }
    }

    @Nested
    @DisplayName("검증 실패 흐름")
    class ValidationFailure {

        @Test
        @DisplayName("PropertyNotFoundException 전파")
        void shouldPropagatePropertyNotFoundException() {
            var command = SetPropertyAmenitiesCommandFixture.aSetPropertyAmenitiesCommand();
            willThrow(new PropertyNotFoundException()).given(validator).validate(command.propertyId());

            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(PropertyNotFoundException.class);
        }

        @Test
        @DisplayName("Validator 실패 시 후속 호출 없음")
        void shouldNotCallOtherManagersWhenValidationFails() {
            var command = SetPropertyAmenitiesCommandFixture.aSetPropertyAmenitiesCommand();
            willThrow(new PropertyNotFoundException()).given(validator).validate(command.propertyId());

            try { service.execute(command); } catch (PropertyNotFoundException ignored) {}

            then(propertyAmenityReadManager).should(never()).getByPropertyId(any());
            then(propertyAmenityFactory).should(never()).createAmenities(any());
            then(propertyAmenityCommandManager).should(never()).persistAll(anyList());
        }
    }

    @Nested
    @DisplayName("호출 순서 검증")
    class InvocationOrder {

        @Test
        @DisplayName("validate -> getByPropertyId -> createAmenities 순서로 호출된다")
        void shouldCallInCorrectOrder() {
            var command = SetPropertyAmenitiesCommandFixture.aSetPropertyAmenitiesCommand();
            var existing = PropertyAmenities.reconstitute(List.of());
            var newAmenities = PropertyAmenities.forNew(List.of());

            willDoNothing().given(validator).validate(command.propertyId());
            given(propertyAmenityReadManager.getByPropertyId(command.propertyId())).willReturn(existing);
            given(propertyAmenityFactory.createAmenities(command)).willReturn(newAmenities);

            service.execute(command);

            InOrder inOrder = inOrder(validator, propertyAmenityReadManager, propertyAmenityFactory);
            inOrder.verify(validator).validate(command.propertyId());
            inOrder.verify(propertyAmenityReadManager).getByPropertyId(command.propertyId());
            inOrder.verify(propertyAmenityFactory).createAmenities(command);
        }
    }
}
