package com.ryuqq.otatoy.application.pricing.validator;

import com.ryuqq.otatoy.application.pricing.dto.command.SetRateAndInventoryCommand;
import com.ryuqq.otatoy.application.pricing.manager.RatePlanReadManager;
import com.ryuqq.otatoy.domain.pricing.PricingFixtures;
import com.ryuqq.otatoy.domain.pricing.RatePlanNotFoundException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;

/**
 * SetRateAndInventoryValidator 단위 테스트.
 * RatePlanReadManager.verifyExists() 위임 및 예외 전파를 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class SetRateAndInventoryValidatorTest {

    @Mock RatePlanReadManager ratePlanReadManager;
    @InjectMocks SetRateAndInventoryValidator validator;

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

    @Nested
    @DisplayName("검증 성공")
    class ValidationSuccess {

        @Test
        @DisplayName("존재하는 RatePlanId로 검증 시 예외가 발생하지 않는다")
        void shouldPassWhenRatePlanExists() {
            // given
            var command = defaultCommand();
            willDoNothing().given(ratePlanReadManager).verifyExists(command.ratePlanId());

            // when & then
            assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("RatePlanReadManager.verifyExists()에 Command의 ratePlanId가 전달된다")
        void shouldDelegateToRatePlanReadManager() {
            // given
            var command = defaultCommand();
            willDoNothing().given(ratePlanReadManager).verifyExists(command.ratePlanId());

            // when
            validator.validate(command);

            // then
            then(ratePlanReadManager).should().verifyExists(command.ratePlanId());
        }
    }

    @Nested
    @DisplayName("검증 실패")
    class ValidationFailure {

        @Test
        @DisplayName("존재하지 않는 RatePlanId로 검증 시 RatePlanNotFoundException이 전파된다")
        void shouldThrowWhenRatePlanNotFound() {
            // given
            var command = defaultCommand();
            willThrow(new RatePlanNotFoundException())
                .given(ratePlanReadManager).verifyExists(command.ratePlanId());

            // when & then
            assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(RatePlanNotFoundException.class);
        }
    }
}
