package com.ryuqq.otatoy.domain.pricing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static com.ryuqq.otatoy.domain.pricing.PricingFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RateRuleTest {

    @Nested
    @DisplayName("T-1: forNew() 생성 검증")
    class Creation {

        @Test
        @DisplayName("정상 생성 시 모든 필드가 올바르게 설정된다")
        void shouldCreateRateRuleSuccessfully() {
            RateRule rule = defaultRateRule();

            assertThat(rule.id().isNew()).isTrue();
            assertThat(rule.ratePlanId()).isEqualTo(RATE_PLAN_ID);
            assertThat(rule.startDate()).isEqualTo(LocalDate.of(2026, 4, 1));
            assertThat(rule.endDate()).isEqualTo(LocalDate.of(2026, 4, 30));
            assertThat(rule.basePrice()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
            assertThat(rule.weekdayPrice()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
            assertThat(rule.fridayPrice()).isEqualByComparingTo(BigDecimal.valueOf(120_000));
            assertThat(rule.saturdayPrice()).isEqualByComparingTo(BigDecimal.valueOf(150_000));
            assertThat(rule.sundayPrice()).isEqualByComparingTo(BigDecimal.valueOf(110_000));
            assertThat(rule.createdAt()).isEqualTo(NOW);
            assertThat(rule.updatedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("startDate가 null이면 예외를 던진다")
        void shouldFailWhenStartDateIsNull() {
            assertThatThrownBy(() -> RateRule.forNew(
                    RATE_PLAN_ID, null, LocalDate.of(2026, 4, 30),
                    BigDecimal.valueOf(100_000), null, null, null, null, NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("시작일과 종료일은 필수");
        }

        @Test
        @DisplayName("endDate가 null이면 예외를 던진다")
        void shouldFailWhenEndDateIsNull() {
            assertThatThrownBy(() -> RateRule.forNew(
                    RATE_PLAN_ID, LocalDate.of(2026, 4, 1), null,
                    BigDecimal.valueOf(100_000), null, null, null, null, NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("시작일과 종료일은 필수");
        }

        @Test
        @DisplayName("endDate가 startDate보다 이전이면 예외를 던진다")
        void shouldFailWhenEndDateBeforeStartDate() {
            assertThatThrownBy(() -> RateRule.forNew(
                    RATE_PLAN_ID,
                    LocalDate.of(2026, 4, 30), LocalDate.of(2026, 4, 1),
                    BigDecimal.valueOf(100_000), null, null, null, null, NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("종료일은 시작일 이후");
        }

        @Test
        @DisplayName("startDate == endDate는 허용된다 (1일짜리 규칙)")
        void shouldAllowSameStartAndEndDate() {
            LocalDate sameDate = LocalDate.of(2026, 4, 15);
            RateRule rule = RateRule.forNew(
                    RATE_PLAN_ID, sameDate, sameDate,
                    BigDecimal.valueOf(100_000), null, null, null, null, NOW
            );

            assertThat(rule.startDate()).isEqualTo(sameDate);
            assertThat(rule.endDate()).isEqualTo(sameDate);
        }

        @Test
        @DisplayName("basePrice가 null이면 예외를 던진다")
        void shouldFailWhenBasePriceIsNull() {
            assertThatThrownBy(() -> RateRule.forNew(
                    RATE_PLAN_ID,
                    LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30),
                    null, null, null, null, null, NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("기본 가격은 0 이상");
        }

        @Test
        @DisplayName("basePrice가 음수이면 예외를 던진다")
        void shouldFailWhenBasePriceIsNegative() {
            assertThatThrownBy(() -> RateRule.forNew(
                    RATE_PLAN_ID,
                    LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30),
                    BigDecimal.valueOf(-1), null, null, null, null, NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("기본 가격은 0 이상");
        }

        @Test
        @DisplayName("basePrice가 0이면 허용된다 (무료 객실)")
        void shouldAllowZeroBasePrice() {
            RateRule rule = RateRule.forNew(
                    RATE_PLAN_ID,
                    LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30),
                    BigDecimal.ZERO, null, null, null, null, NOW
            );

            assertThat(rule.basePrice()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("T-4: calculatePrice() 요일별 가격 계산")
    class CalculatePrice {

        @Test
        @DisplayName("월요일은 weekdayPrice를 반환한다")
        void shouldReturnWeekdayPriceOnMonday() {
            RateRule rule = defaultRateRule();
            // 2026-04-06은 월요일
            LocalDate monday = LocalDate.of(2026, 4, 6);

            assertThat(rule.calculatePrice(monday))
                    .isEqualByComparingTo(BigDecimal.valueOf(100_000));
        }

        @Test
        @DisplayName("화요일은 weekdayPrice를 반환한다")
        void shouldReturnWeekdayPriceOnTuesday() {
            RateRule rule = defaultRateRule();
            LocalDate tuesday = LocalDate.of(2026, 4, 7);

            assertThat(rule.calculatePrice(tuesday))
                    .isEqualByComparingTo(BigDecimal.valueOf(100_000));
        }

        @Test
        @DisplayName("금요일은 fridayPrice를 반환한다")
        void shouldReturnFridayPriceOnFriday() {
            RateRule rule = defaultRateRule();
            // 2026-04-03은 금요일
            LocalDate friday = LocalDate.of(2026, 4, 3);

            assertThat(rule.calculatePrice(friday))
                    .isEqualByComparingTo(BigDecimal.valueOf(120_000));
        }

        @Test
        @DisplayName("토요일은 saturdayPrice를 반환한다")
        void shouldReturnSaturdayPriceOnSaturday() {
            RateRule rule = defaultRateRule();
            // 2026-04-04는 토요일
            LocalDate saturday = LocalDate.of(2026, 4, 4);

            assertThat(rule.calculatePrice(saturday))
                    .isEqualByComparingTo(BigDecimal.valueOf(150_000));
        }

        @Test
        @DisplayName("일요일은 sundayPrice를 반환한다")
        void shouldReturnSundayPriceOnSunday() {
            RateRule rule = defaultRateRule();
            // 2026-04-05는 일요일
            LocalDate sunday = LocalDate.of(2026, 4, 5);

            assertThat(rule.calculatePrice(sunday))
                    .isEqualByComparingTo(BigDecimal.valueOf(110_000));
        }

        @Test
        @DisplayName("weekdayPrice가 null이면 basePrice로 폴백한다")
        void shouldFallbackToBasePriceWhenWeekdayPriceIsNull() {
            RateRule rule = basePriceOnlyRateRule();
            LocalDate monday = LocalDate.of(2026, 4, 6);

            assertThat(rule.calculatePrice(monday))
                    .isEqualByComparingTo(BigDecimal.valueOf(80_000));
        }

        @Test
        @DisplayName("fridayPrice가 null이면 basePrice로 폴백한다")
        void shouldFallbackToBasePriceWhenFridayPriceIsNull() {
            RateRule rule = basePriceOnlyRateRule();
            LocalDate friday = LocalDate.of(2026, 4, 3);

            assertThat(rule.calculatePrice(friday))
                    .isEqualByComparingTo(BigDecimal.valueOf(80_000));
        }

        @Test
        @DisplayName("saturdayPrice가 null이면 basePrice로 폴백한다")
        void shouldFallbackToBasePriceWhenSaturdayPriceIsNull() {
            RateRule rule = basePriceOnlyRateRule();
            LocalDate saturday = LocalDate.of(2026, 4, 4);

            assertThat(rule.calculatePrice(saturday))
                    .isEqualByComparingTo(BigDecimal.valueOf(80_000));
        }

        @Test
        @DisplayName("sundayPrice가 null이면 basePrice로 폴백한다")
        void shouldFallbackToBasePriceWhenSundayPriceIsNull() {
            RateRule rule = basePriceOnlyRateRule();
            LocalDate sunday = LocalDate.of(2026, 4, 5);

            assertThat(rule.calculatePrice(sunday))
                    .isEqualByComparingTo(BigDecimal.valueOf(80_000));
        }

        @Test
        @DisplayName("범위 밖 날짜 조회 시 예외를 던진다")
        void shouldFailWhenDateOutOfRange() {
            RateRule rule = defaultRateRule();
            LocalDate outOfRange = LocalDate.of(2026, 5, 1);

            assertThatThrownBy(() -> rule.calculatePrice(outOfRange))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("요금 규칙 범위 밖");
        }

        @Test
        @DisplayName("시작일 당일은 범위 안이다")
        void shouldAcceptStartDate() {
            RateRule rule = defaultRateRule();
            LocalDate startDate = LocalDate.of(2026, 4, 1);

            assertThat(rule.calculatePrice(startDate)).isNotNull();
        }

        @Test
        @DisplayName("종료일 당일은 범위 안이다")
        void shouldAcceptEndDate() {
            RateRule rule = defaultRateRule();
            LocalDate endDate = LocalDate.of(2026, 4, 30);

            assertThat(rule.calculatePrice(endDate)).isNotNull();
        }
    }

    @Nested
    @DisplayName("T-4: resolvePrice() 오버라이드 반영 가격 계산")
    class ResolvePrice {

        @Test
        @DisplayName("오버라이드가 존재하는 날짜면 오버라이드 가격을 반환한다")
        void shouldReturnOverridePriceWhenOverrideExists() {
            RateRule rule = defaultRateRule();
            List<RateOverride> overrides = overrideListWithApril5();
            LocalDate april5 = LocalDate.of(2026, 4, 5);

            assertThat(rule.resolvePrice(april5, overrides))
                    .isEqualByComparingTo(BigDecimal.valueOf(170_000));
        }

        @Test
        @DisplayName("오버라이드가 없는 날짜면 요일 기반 가격을 반환한다")
        void shouldReturnCalculatedPriceWhenNoOverride() {
            RateRule rule = defaultRateRule();
            List<RateOverride> overrides = overrideListWithApril5();
            // 2026-04-06 월요일 -- 오버라이드 없음
            LocalDate monday = LocalDate.of(2026, 4, 6);

            assertThat(rule.resolvePrice(monday, overrides))
                    .isEqualByComparingTo(BigDecimal.valueOf(100_000));
        }

        @Test
        @DisplayName("오버라이드 리스트가 null이면 요일 기반 가격을 반환한다")
        void shouldReturnCalculatedPriceWhenOverridesIsNull() {
            RateRule rule = defaultRateRule();
            LocalDate saturday = LocalDate.of(2026, 4, 4);

            assertThat(rule.resolvePrice(saturday, null))
                    .isEqualByComparingTo(BigDecimal.valueOf(150_000));
        }

        @Test
        @DisplayName("오버라이드 리스트가 빈 리스트이면 요일 기반 가격을 반환한다")
        void shouldReturnCalculatedPriceWhenOverridesIsEmpty() {
            RateRule rule = defaultRateRule();
            LocalDate friday = LocalDate.of(2026, 4, 3);

            assertThat(rule.resolvePrice(friday, Collections.emptyList()))
                    .isEqualByComparingTo(BigDecimal.valueOf(120_000));
        }

        @Test
        @DisplayName("범위 밖 날짜로 resolvePrice 호출 시 예외를 던진다")
        void shouldFailWhenDateOutOfRangeInResolvePrice() {
            RateRule rule = defaultRateRule();
            LocalDate outOfRange = LocalDate.of(2026, 5, 1);

            assertThatThrownBy(() -> rule.resolvePrice(outOfRange, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("요금 규칙 범위 밖");
        }
    }

    @Nested
    @DisplayName("T-4: covers() 범위 판단")
    class Covers {

        @Test
        @DisplayName("시작일 당일은 covers=true")
        void shouldCoverStartDate() {
            RateRule rule = defaultRateRule();
            assertThat(rule.covers(LocalDate.of(2026, 4, 1))).isTrue();
        }

        @Test
        @DisplayName("종료일 당일은 covers=true")
        void shouldCoverEndDate() {
            RateRule rule = defaultRateRule();
            assertThat(rule.covers(LocalDate.of(2026, 4, 30))).isTrue();
        }

        @Test
        @DisplayName("범위 내 날짜는 covers=true")
        void shouldCoverMiddleDate() {
            RateRule rule = defaultRateRule();
            assertThat(rule.covers(LocalDate.of(2026, 4, 15))).isTrue();
        }

        @Test
        @DisplayName("시작일 하루 전은 covers=false")
        void shouldNotCoverDayBeforeStart() {
            RateRule rule = defaultRateRule();
            assertThat(rule.covers(LocalDate.of(2026, 3, 31))).isFalse();
        }

        @Test
        @DisplayName("종료일 하루 후는 covers=false")
        void shouldNotCoverDayAfterEnd() {
            RateRule rule = defaultRateRule();
            assertThat(rule.covers(LocalDate.of(2026, 5, 1))).isFalse();
        }
    }

    @Nested
    @DisplayName("T-3: reconstitute() 복원")
    class Reconstitute {

        @Test
        @DisplayName("검증 없이 모든 필드를 복원한다")
        void shouldReconstituteFaithfully() {
            RateRule rule = reconstitutedRateRule(42L);

            assertThat(rule.id()).isEqualTo(RateRuleId.of(42L));
            assertThat(rule.ratePlanId()).isEqualTo(RATE_PLAN_ID);
            assertThat(rule.basePrice()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
        }
    }

    @Nested
    @DisplayName("T-6: equals/hashCode")
    class EqualsHashCode {

        @Test
        @DisplayName("같은 ID의 RateRule은 동등하다")
        void shouldBeEqualWithSameId() {
            RateRule a = reconstitutedRateRule(1L);
            RateRule b = reconstitutedRateRule(1L);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("다른 ID의 RateRule은 동등하지 않다")
        void shouldNotBeEqualWithDifferentId() {
            RateRule a = reconstitutedRateRule(1L);
            RateRule b = reconstitutedRateRule(2L);

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("forNew()로 생성된 RateRule은 id가 RateRuleId(null)이므로 Record 동등성에 의해 equals=true")
        void shouldBeEqualWhenBothIdsAreNewWithIdVo() {
            // RateRule.forNew()는 RateRuleId.of(null)을 사용하므로
            // id 필드가 null이 아닌 RateRuleId(null) Record 객체.
            // Record의 equals는 value 기반이므로 두 RateRuleId(null)은 동등.
            // 이는 RatePlan(id에 raw null)과 다른 동작이며, MINOR 이슈로 기록됨.
            RateRule a = defaultRateRule();
            RateRule b = defaultRateRule();

            assertThat(a).isEqualTo(b);
        }
    }
}
