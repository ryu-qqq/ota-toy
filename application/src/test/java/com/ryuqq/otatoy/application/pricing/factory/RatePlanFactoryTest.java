package com.ryuqq.otatoy.application.pricing.factory;

import com.ryuqq.otatoy.application.common.factory.TimeProvider;
import com.ryuqq.otatoy.application.pricing.dto.command.RegisterRatePlanCommand;
import com.ryuqq.otatoy.application.pricing.dto.command.RegisterRatePlanCommandFixture;
import com.ryuqq.otatoy.domain.pricing.PaymentPolicy;
import com.ryuqq.otatoy.domain.pricing.RatePlan;
import com.ryuqq.otatoy.domain.pricing.SourceType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * RatePlanFactory 단위 테스트.
 * TimeProvider를 Mock하여 시간 일원화 및 DIRECT/null 고정을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class RatePlanFactoryTest {

    @Mock TimeProvider timeProvider;
    @InjectMocks RatePlanFactory factory;

    private static final Instant FIXED_NOW = Instant.parse("2026-04-06T00:00:00Z");

    @Nested
    @DisplayName("createForDirect")
    class CreateForDirect {

        @Test
        @DisplayName("생성된 RatePlan의 sourceType은 DIRECT이다")
        void shouldSetSourceTypeToDirect() {
            // given
            RegisterRatePlanCommand command = RegisterRatePlanCommandFixture.aRegisterRatePlanCommand();
            given(timeProvider.now()).willReturn(FIXED_NOW);

            // when
            RatePlan result = factory.createForDirect(command);

            // then
            assertThat(result.sourceType()).isEqualTo(SourceType.DIRECT);
        }

        @Test
        @DisplayName("생성된 RatePlan의 supplierId는 null이다")
        void shouldSetSupplierIdToNull() {
            // given
            RegisterRatePlanCommand command = RegisterRatePlanCommandFixture.aRegisterRatePlanCommand();
            given(timeProvider.now()).willReturn(FIXED_NOW);

            // when
            RatePlan result = factory.createForDirect(command);

            // then
            assertThat(result.supplierId()).isNull();
        }

        @Test
        @DisplayName("Command의 roomTypeId, name, cancellationPolicy, paymentPolicy가 그대로 전달된다")
        void shouldMapCommandFieldsCorrectly() {
            // given
            RegisterRatePlanCommand command = RegisterRatePlanCommandFixture.aRegisterRatePlanCommand();
            given(timeProvider.now()).willReturn(FIXED_NOW);

            // when
            RatePlan result = factory.createForDirect(command);

            // then
            assertThat(result.roomTypeId()).isEqualTo(command.roomTypeId());
            assertThat(result.name()).isEqualTo(command.name());
            assertThat(result.cancellationPolicy()).isEqualTo(command.cancellationPolicy());
            assertThat(result.paymentPolicy()).isEqualTo(command.paymentPolicy());
        }

        @Test
        @DisplayName("TimeProvider.now()의 반환값이 createdAt에 사용된다")
        void shouldUseTimeProviderForTimestamp() {
            // given
            RegisterRatePlanCommand command = RegisterRatePlanCommandFixture.aRegisterRatePlanCommand();
            given(timeProvider.now()).willReturn(FIXED_NOW);

            // when
            RatePlan result = factory.createForDirect(command);

            // then
            assertThat(result.createdAt()).isEqualTo(FIXED_NOW);
        }
    }
}
