package com.ryuqq.otatoy.application.property.service;

import com.ryuqq.otatoy.application.property.dto.command.SetPropertyPhotosCommand;
import com.ryuqq.otatoy.application.property.dto.command.SetPropertyPhotosCommandFixture;
import com.ryuqq.otatoy.application.property.factory.PropertyPhotoFactory;
import com.ryuqq.otatoy.application.property.manager.PropertyPhotoCommandManager;
import com.ryuqq.otatoy.application.property.manager.PropertyPhotoReadManager;
import com.ryuqq.otatoy.application.property.validator.PropertyPhotosValidator;
import com.ryuqq.otatoy.domain.property.PropertyNotFoundException;
import com.ryuqq.otatoy.domain.property.PropertyPhotos;

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

/**
 * SetPropertyPhotosService 단위 테스트.
 * Validator, Factory, ReadManager, CommandManager를 Mock으로 대체하여
 * Service의 오케스트레이션 로직을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class SetPropertyPhotosServiceTest {

    @Mock PropertyPhotosValidator validator;
    @Mock PropertyPhotoFactory propertyPhotoFactory;
    @Mock PropertyPhotoReadManager propertyPhotoReadManager;
    @Mock PropertyPhotoCommandManager propertyPhotoCommandManager;
    @InjectMocks SetPropertyPhotosService service;

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("빈 목록끼리 비교하면 변경 없으므로 persistAll 호출 안 됨")
        void shouldNotPersistWhenNoChanges() {
            // given
            var command = SetPropertyPhotosCommandFixture.aSetPropertyPhotosCommand();
            var existing = PropertyPhotos.reconstitute(List.of());
            var newPhotos = PropertyPhotos.forNew(List.of());

            willDoNothing().given(validator).validate(command.propertyId());
            given(propertyPhotoReadManager.getByPropertyId(command.propertyId())).willReturn(existing);
            given(propertyPhotoFactory.createPhotos(command)).willReturn(newPhotos);

            // when
            service.execute(command);

            // then
            then(propertyPhotoCommandManager).should(never()).persistAll(anyList());
        }
    }

    @Nested
    @DisplayName("검증 실패 흐름")
    class ValidationFailure {

        @Test
        @DisplayName("PropertyNotFoundException 전파")
        void shouldPropagatePropertyNotFoundException() {
            var command = SetPropertyPhotosCommandFixture.aSetPropertyPhotosCommand();
            willThrow(new PropertyNotFoundException()).given(validator).validate(command.propertyId());

            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(PropertyNotFoundException.class);
        }

        @Test
        @DisplayName("Validator 실패 시 후속 호출 없음")
        void shouldNotCallOtherManagersWhenValidationFails() {
            var command = SetPropertyPhotosCommandFixture.aSetPropertyPhotosCommand();
            willThrow(new PropertyNotFoundException()).given(validator).validate(command.propertyId());

            try { service.execute(command); } catch (PropertyNotFoundException ignored) {}

            then(propertyPhotoReadManager).should(never()).getByPropertyId(any());
            then(propertyPhotoFactory).should(never()).createPhotos(any());
            then(propertyPhotoCommandManager).should(never()).persistAll(anyList());
        }
    }

    @Nested
    @DisplayName("호출 순서 검증")
    class InvocationOrder {

        @Test
        @DisplayName("validate -> getByPropertyId -> createPhotos 순서로 호출된다")
        void shouldCallInCorrectOrder() {
            var command = SetPropertyPhotosCommandFixture.aSetPropertyPhotosCommand();
            var existing = PropertyPhotos.reconstitute(List.of());
            var newPhotos = PropertyPhotos.forNew(List.of());

            willDoNothing().given(validator).validate(command.propertyId());
            given(propertyPhotoReadManager.getByPropertyId(command.propertyId())).willReturn(existing);
            given(propertyPhotoFactory.createPhotos(command)).willReturn(newPhotos);

            service.execute(command);

            InOrder inOrder = inOrder(validator, propertyPhotoReadManager, propertyPhotoFactory);
            inOrder.verify(validator).validate(command.propertyId());
            inOrder.verify(propertyPhotoReadManager).getByPropertyId(command.propertyId());
            inOrder.verify(propertyPhotoFactory).createPhotos(command);
        }
    }
}
