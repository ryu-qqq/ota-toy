package com.ryuqq.otatoy.application.pricing.service;

import com.ryuqq.otatoy.application.pricing.dto.RateAndInventoryBundle;
import com.ryuqq.otatoy.application.pricing.dto.command.SetRateAndInventoryCommand;
import com.ryuqq.otatoy.application.pricing.facade.RateAndInventoryPersistenceFacade;
import com.ryuqq.otatoy.application.pricing.factory.RateAndInventoryFactory;
import com.ryuqq.otatoy.application.pricing.manager.RatePlanReadManager;
import com.ryuqq.otatoy.application.pricing.validator.SetRateAndInventoryValidator;
import com.ryuqq.otatoy.domain.pricing.PricingFixtures;
import com.ryuqq.otatoy.domain.pricing.RatePlan;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.domain.pricing.RatePlanNotFoundException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

/**
 * SetRateAndInventoryService 단위 테스트.
 * Service가 Validator -> RatePlanReadManager -> Factory -> PersistenceFacade 순서로
 * 올바르게 조합하는지 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@ExtendWith(MockitoExtension.class)
class SetRateAndInventoryServiceTest {

    @Mock
    SetRateAndInventoryValidator validator;

    @Mock
    RatePlanReadManager ratePlanReadManager;

    @Mock
    RateAndInventoryFactory factory;

    @Mock
    RateAndInventoryPersistenceFacade persistenceFacade;

    @InjectMocks
    SetRateAndInventoryService service;

    // -- 테스트용 커맨드/번들 헬퍼 --

    private static SetRateAndInventoryCommand defaultCommand() {
        return new SetRateAndInventoryCommand(
            PricingFixtures.RATE_PLAN_ID,
            LocalDate.of(2026, 4, 1),
            LocalDate.of(2026, 4, 3),
            BigDecimal.valueOf(100_000),
            BigDecimal.valueOf(100_000),
            BigDecimal.valueOf(120_000),
            BigDecimal.valueOf(150_000),
            BigDecimal.valueOf(110_000),
            10,
            List.of()
        );
    }

    private static SetRateAndInventoryCommand commandWithOverrides() {
        return new SetRateAndInventoryCommand(
            PricingFixtures.RATE_PLAN_ID,
            LocalDate.of(2026, 4, 1),
            LocalDate.of(2026, 4, 3),
            BigDecimal.valueOf(100_000),
            BigDecimal.valueOf(100_000),
            BigDecimal.valueOf(120_000),
            BigDecimal.valueOf(150_000),
            BigDecimal.valueOf(110_000),
            10,
            List.of(
                new SetRateAndInventoryCommand.OverrideItem(
                    LocalDate.of(2026, 4, 2),
                    BigDecimal.valueOf(170_000),
                    "공휴일 특가"
                )
            )
        );
    }

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("유효한 커맨드로 요금/재고 설정 시 validate -> getById -> createBundle -> persist 순서로 실행된다")
        void shouldExecuteFullFlowSuccessfully() {
            // given
            var command = defaultCommand();
            var ratePlan = PricingFixtures.reconstitutedRatePlan(1L);
            var bundle = mock(RateAndInventoryBundle.class);

            willDoNothing().given(validator).validate(command);
            given(ratePlanReadManager.getById(command.ratePlanId())).willReturn(ratePlan);
            given(factory.createBundle(command, ratePlan)).willReturn(bundle);
            willDoNothing().given(persistenceFacade).persist(bundle);

            // when
            service.execute(command);

            // then
            then(validator).should().validate(command);
            then(ratePlanReadManager).should().getById(command.ratePlanId());
            then(factory).should().createBundle(command, ratePlan);
            then(persistenceFacade).should().persist(bundle);
        }

        @Test
        @DisplayName("오버라이드가 포함된 커맨드도 정상적으로 처리된다")
        void shouldHandleCommandWithOverrides() {
            // given
            var command = commandWithOverrides();
            var ratePlan = PricingFixtures.reconstitutedRatePlan(1L);
            var bundle = mock(RateAndInventoryBundle.class);

            willDoNothing().given(validator).validate(command);
            given(ratePlanReadManager.getById(command.ratePlanId())).willReturn(ratePlan);
            given(factory.createBundle(command, ratePlan)).willReturn(bundle);
            willDoNothing().given(persistenceFacade).persist(bundle);

            // when
            service.execute(command);

            // then
            then(factory).should().createBundle(command, ratePlan);
            then(persistenceFacade).should().persist(bundle);
        }
    }

    @Nested
    @DisplayName("실패 흐름")
    class Failure {

        @Test
        @DisplayName("존재하지 않는 RatePlan으로 설정 시 RatePlanNotFoundException이 전파된다")
        void shouldPropagateRatePlanNotFoundException() {
            // given
            var command = defaultCommand();
            willThrow(new RatePlanNotFoundException()).given(validator).validate(command);

            // when & then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(RatePlanNotFoundException.class);
        }

        @Test
        @DisplayName("Validator 실패 시 RatePlanReadManager, Factory, Facade 모두 호출되지 않는다")
        void shouldNotCallSubsequentComponentsWhenValidationFails() {
            // given
            var command = defaultCommand();
            willThrow(new RatePlanNotFoundException()).given(validator).validate(command);

            // when
            try {
                service.execute(command);
            } catch (RatePlanNotFoundException ignored) {
            }

            // then
            then(ratePlanReadManager).should(never()).getById(any());
            then(factory).should(never()).createBundle(any(), any());
            then(persistenceFacade).should(never()).persist(any());
        }

        @Test
        @DisplayName("Factory에서 예외 발생 시 PersistenceFacade는 호출되지 않는다")
        void shouldNotCallFacadeWhenFactoryFails() {
            // given
            var command = defaultCommand();
            var ratePlan = PricingFixtures.reconstitutedRatePlan(1L);

            willDoNothing().given(validator).validate(command);
            given(ratePlanReadManager.getById(command.ratePlanId())).willReturn(ratePlan);
            given(factory.createBundle(command, ratePlan))
                .willThrow(new IllegalArgumentException("요금 생성 실패"));

            // when
            try {
                service.execute(command);
            } catch (IllegalArgumentException ignored) {
            }

            // then
            then(persistenceFacade).should(never()).persist(any());
        }

        @Test
        @DisplayName("RatePlanReadManager.getById() 실패 시 Factory와 Facade는 호출되지 않는다")
        void shouldNotCallFactoryOrFacadeWhenGetByIdFails() {
            // given
            var command = defaultCommand();

            willDoNothing().given(validator).validate(command);
            given(ratePlanReadManager.getById(command.ratePlanId()))
                .willThrow(new RatePlanNotFoundException());

            // when
            try {
                service.execute(command);
            } catch (RatePlanNotFoundException ignored) {
            }

            // then
            then(factory).should(never()).createBundle(any(), any());
            then(persistenceFacade).should(never()).persist(any());
        }
    }

    @Nested
    @DisplayName("호출 순서 검증")
    class InvocationOrder {

        @Test
        @DisplayName("validate() -> getById() -> createBundle() -> persist() 순서로 호출된다")
        void shouldCallInCorrectOrder() {
            // given
            var command = defaultCommand();
            var ratePlan = PricingFixtures.reconstitutedRatePlan(1L);
            var bundle = mock(RateAndInventoryBundle.class);

            willDoNothing().given(validator).validate(command);
            given(ratePlanReadManager.getById(command.ratePlanId())).willReturn(ratePlan);
            given(factory.createBundle(command, ratePlan)).willReturn(bundle);
            willDoNothing().given(persistenceFacade).persist(bundle);

            // when
            service.execute(command);

            // then
            InOrder inOrder = inOrder(validator, ratePlanReadManager, factory, persistenceFacade);
            inOrder.verify(validator).validate(command);
            inOrder.verify(ratePlanReadManager).getById(command.ratePlanId());
            inOrder.verify(factory).createBundle(command, ratePlan);
            inOrder.verify(persistenceFacade).persist(bundle);
        }
    }

    @Nested
    @DisplayName("Facade 인자 검증")
    class FacadeArgumentVerification {

        @Test
        @DisplayName("Factory가 생성한 번들이 PersistenceFacade에 그대로 전달된다")
        void shouldPassExactBundleToFacade() {
            // given
            var command = defaultCommand();
            var ratePlan = PricingFixtures.reconstitutedRatePlan(1L);
            var bundle = mock(RateAndInventoryBundle.class);

            willDoNothing().given(validator).validate(command);
            given(ratePlanReadManager.getById(command.ratePlanId())).willReturn(ratePlan);
            given(factory.createBundle(command, ratePlan)).willReturn(bundle);
            willDoNothing().given(persistenceFacade).persist(bundle);

            // when
            service.execute(command);

            // then -- 동일한 bundle 인스턴스가 전달되었는지 검증
            then(persistenceFacade).should().persist(bundle);
        }
    }
}
