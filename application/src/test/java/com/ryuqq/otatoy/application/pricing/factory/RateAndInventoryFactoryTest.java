package com.ryuqq.otatoy.application.pricing.factory;

import com.ryuqq.otatoy.application.common.factory.TimeProvider;
import com.ryuqq.otatoy.application.pricing.dto.RateAndInventoryBundle;
import com.ryuqq.otatoy.application.pricing.dto.command.SetRateAndInventoryCommand;
import com.ryuqq.otatoy.domain.pricing.PricingFixtures;
import com.ryuqq.otatoy.domain.pricing.RatePlan;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * RateAndInventoryFactory 단위 테스트.
 * TimeProvider를 Mock하여 RateRule, RateOverride, Rate, Inventory 번들 생성을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class RateAndInventoryFactoryTest {

    @Mock TimeProvider timeProvider;
    @InjectMocks RateAndInventoryFactory factory;

    private static final Instant FIXED_NOW = Instant.parse("2026-04-06T00:00:00Z");

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
    @DisplayName("RateRule 생성")
    class RateRuleCreation {

        @Test
        @DisplayName("Command의 날짜 범위와 가격 정보가 RateRule에 반영된다")
        void shouldCreateRateRuleWithCommandValues() {
            // given
            var command = defaultCommand();
            RatePlan ratePlan = PricingFixtures.reconstitutedRatePlan(1L);
            given(timeProvider.now()).willReturn(FIXED_NOW);

            // when
            RateAndInventoryBundle bundle = factory.createBundle(command, ratePlan);

            // then
            assertThat(bundle.rateRule()).isNotNull();
            assertThat(bundle.rateRule().ratePlanId()).isEqualTo(command.ratePlanId());
            assertThat(bundle.rateRule().startDate()).isEqualTo(command.startDate());
            assertThat(bundle.rateRule().endDate()).isEqualTo(command.endDate());
        }
    }

    @Nested
    @DisplayName("RateOverride 생성")
    class RateOverrideCreation {

        @Test
        @DisplayName("오버라이드가 없으면 빈 리스트를 반환한다")
        void shouldReturnEmptyOverridesWhenNone() {
            // given
            var command = defaultCommand();
            RatePlan ratePlan = PricingFixtures.reconstitutedRatePlan(1L);
            given(timeProvider.now()).willReturn(FIXED_NOW);

            // when
            RateAndInventoryBundle bundle = factory.createBundle(command, ratePlan);

            // then
            assertThat(bundle.overrides()).isEmpty();
        }

        @Test
        @DisplayName("오버라이드가 있으면 pending 상태(rateRuleId=null)로 생성된다")
        void shouldCreatePendingOverrides() {
            // given
            var command = commandWithOverrides();
            RatePlan ratePlan = PricingFixtures.reconstitutedRatePlan(1L);
            given(timeProvider.now()).willReturn(FIXED_NOW);

            // when
            RateAndInventoryBundle bundle = factory.createBundle(command, ratePlan);

            // then
            assertThat(bundle.overrides()).hasSize(1);
            assertThat(bundle.overrides().get(0).rateRuleId()).isNull();
            assertThat(bundle.overrides().get(0).overrideDate()).isEqualTo(LocalDate.of(2026, 4, 2));
            assertThat(bundle.overrides().get(0).price()).isEqualByComparingTo(BigDecimal.valueOf(170_000));
        }
    }

    @Nested
    @DisplayName("Rate 스냅샷 생성")
    class RateSnapshotCreation {

        @Test
        @DisplayName("시작일부터 종료일까지 날짜별 Rate가 생성된다")
        void shouldGenerateRatesForEachDate() {
            // given -- 4/1 ~ 4/3 -> 3일치
            var command = defaultCommand();
            RatePlan ratePlan = PricingFixtures.reconstitutedRatePlan(1L);
            given(timeProvider.now()).willReturn(FIXED_NOW);

            // when
            RateAndInventoryBundle bundle = factory.createBundle(command, ratePlan);

            // then
            assertThat(bundle.rates()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Inventory 생성")
    class InventoryCreation {

        @Test
        @DisplayName("시작일부터 종료일까지 날짜별 Inventory가 생성된다")
        void shouldCreateInventoryForEachDate() {
            // given -- 4/1 ~ 4/3 -> 3일치
            var command = defaultCommand();
            RatePlan ratePlan = PricingFixtures.reconstitutedRatePlan(1L);
            given(timeProvider.now()).willReturn(FIXED_NOW);

            // when
            RateAndInventoryBundle bundle = factory.createBundle(command, ratePlan);

            // then
            assertThat(bundle.inventories()).hasSize(3);
            assertThat(bundle.inventories().get(0).roomTypeId()).isEqualTo(ratePlan.roomTypeId());
        }

        @Test
        @DisplayName("Inventory의 roomTypeId는 RatePlan에서 가져온 값이다")
        void shouldUseRoomTypeIdFromRatePlan() {
            // given
            var command = defaultCommand();
            RatePlan ratePlan = PricingFixtures.reconstitutedRatePlan(1L);
            given(timeProvider.now()).willReturn(FIXED_NOW);

            // when
            RateAndInventoryBundle bundle = factory.createBundle(command, ratePlan);

            // then
            bundle.inventories().forEach(inv ->
                assertThat(inv.roomTypeId()).isEqualTo(ratePlan.roomTypeId())
            );
        }
    }

    @Nested
    @DisplayName("번들 구성 검증")
    class BundleComposition {

        @Test
        @DisplayName("번들에 RateRule, overrides, rates, inventories가 모두 포함된다")
        void shouldContainAllComponents() {
            // given
            var command = commandWithOverrides();
            RatePlan ratePlan = PricingFixtures.reconstitutedRatePlan(1L);
            given(timeProvider.now()).willReturn(FIXED_NOW);

            // when
            RateAndInventoryBundle bundle = factory.createBundle(command, ratePlan);

            // then
            assertThat(bundle.rateRule()).isNotNull();
            assertThat(bundle.overrides()).isNotEmpty();
            assertThat(bundle.rates()).isNotEmpty();
            assertThat(bundle.inventories()).isNotEmpty();
        }
    }
}
