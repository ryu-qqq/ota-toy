package com.ryuqq.otatoy.application.pricing.service;

import com.ryuqq.otatoy.application.pricing.dto.command.RegisterRatePlanCommand;
import com.ryuqq.otatoy.application.pricing.dto.command.RegisterRatePlanCommandFixture;
import com.ryuqq.otatoy.application.pricing.factory.RatePlanFactory;
import com.ryuqq.otatoy.application.pricing.manager.RatePlanCommandManager;
import com.ryuqq.otatoy.application.pricing.validator.RatePlanRegistrationValidator;
import com.ryuqq.otatoy.domain.pricing.PricingFixtures;
import com.ryuqq.otatoy.domain.pricing.RatePlan;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeNotFoundException;

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
 * RegisterRatePlanService 단위 테스트.
 * Validator, Factory, CommandManager를 Mock으로 대체하여
 * Service의 오케스트레이션 로직을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@ExtendWith(MockitoExtension.class)
class RegisterRatePlanServiceTest {

    @Mock
    RatePlanRegistrationValidator validator;

    @Mock
    RatePlanFactory ratePlanFactory;

    @Mock
    RatePlanCommandManager ratePlanCommandManager;

    @InjectMocks
    RegisterRatePlanService service;

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("유효한 입력으로 RatePlan 등록 성공 시 RatePlanId를 반환한다")
        void shouldRegisterRatePlanAndReturnId() {
            // given
            RegisterRatePlanCommand command = RegisterRatePlanCommandFixture.aRegisterRatePlanCommand();
            RatePlan ratePlan = PricingFixtures.directRatePlan();

            willDoNothing().given(validator).validate(command);
            given(ratePlanFactory.createForDirect(command)).willReturn(ratePlan);
            given(ratePlanCommandManager.persist(ratePlan)).willReturn(1L);

            // when
            Long result = service.execute(command);

            // then
            assertThat(result).isEqualTo(1L);
        }

        @Test
        @DisplayName("Factory가 생성한 RatePlan이 CommandManager에 그대로 전달된다")
        void shouldPassFactoryCreatedRatePlanToCommandManager() {
            // given
            RegisterRatePlanCommand command = RegisterRatePlanCommandFixture.aRegisterRatePlanCommand();
            RatePlan ratePlan = PricingFixtures.directRatePlan();

            willDoNothing().given(validator).validate(command);
            given(ratePlanFactory.createForDirect(command)).willReturn(ratePlan);
            given(ratePlanCommandManager.persist(ratePlan)).willReturn(42L);

            // when
            service.execute(command);

            // then
            then(ratePlanCommandManager).should().persist(ratePlan);
        }
    }

    @Nested
    @DisplayName("검증 실패 흐름")
    class ValidationFailure {

        @Test
        @DisplayName("존재하지 않는 객실 유형으로 등록 시 RoomTypeNotFoundException이 전파된다")
        void shouldPropagateRoomTypeNotFoundException() {
            // given
            RegisterRatePlanCommand command = RegisterRatePlanCommandFixture.aRegisterRatePlanCommand();
            willThrow(new RoomTypeNotFoundException()).given(validator).validate(command);

            // when & then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(RoomTypeNotFoundException.class);
        }

        @Test
        @DisplayName("Validator 실패 시 Factory와 CommandManager는 호출되지 않는다")
        void shouldNotCallFactoryOrManagerWhenValidationFails() {
            // given
            RegisterRatePlanCommand command = RegisterRatePlanCommandFixture.aRegisterRatePlanCommand();
            willThrow(new RoomTypeNotFoundException()).given(validator).validate(command);

            // when
            try {
                service.execute(command);
            } catch (RoomTypeNotFoundException ignored) {
                // 예외는 무시 -- 호출 검증이 목적
            }

            // then
            then(ratePlanFactory).should(never()).createForDirect(any());
            then(ratePlanCommandManager).should(never()).persist(any());
        }
    }

    @Nested
    @DisplayName("호출 순서 검증")
    class InvocationOrder {

        @Test
        @DisplayName("validate() -> createForDirect() -> persist() 순서로 호출된다")
        void shouldCallInCorrectOrder() {
            // given
            RegisterRatePlanCommand command = RegisterRatePlanCommandFixture.aRegisterRatePlanCommand();
            RatePlan ratePlan = PricingFixtures.directRatePlan();

            willDoNothing().given(validator).validate(command);
            given(ratePlanFactory.createForDirect(command)).willReturn(ratePlan);
            given(ratePlanCommandManager.persist(ratePlan)).willReturn(1L);

            // when
            service.execute(command);

            // then
            InOrder inOrder = inOrder(validator, ratePlanFactory, ratePlanCommandManager);
            inOrder.verify(validator).validate(command);
            inOrder.verify(ratePlanFactory).createForDirect(command);
            inOrder.verify(ratePlanCommandManager).persist(ratePlan);
        }

        @Test
        @DisplayName("Validator가 가장 먼저 호출되어야 한다")
        void shouldCallValidatorFirst() {
            // given
            RegisterRatePlanCommand command = RegisterRatePlanCommandFixture.aRegisterRatePlanCommand();
            RatePlan ratePlan = PricingFixtures.directRatePlan();

            willDoNothing().given(validator).validate(command);
            given(ratePlanFactory.createForDirect(command)).willReturn(ratePlan);
            given(ratePlanCommandManager.persist(ratePlan)).willReturn(1L);

            // when
            service.execute(command);

            // then
            InOrder inOrder = inOrder(validator, ratePlanFactory);
            inOrder.verify(validator).validate(command);
            inOrder.verify(ratePlanFactory).createForDirect(command);
        }
    }
}
