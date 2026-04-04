package com.ryuqq.otatoy.domain.pricing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.ryuqq.otatoy.domain.pricing.PricingFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RateTest {

    private static final LocalDate RATE_DATE = LocalDate.of(2026, 4, 10);

    @Nested
    @DisplayName("T-1: forNew() 생성 검증")
    class Creation {

        @Test
        @DisplayName("정상 생성 시 모든 필드가 올바르게 설정된다")
        void shouldCreateRateSuccessfully() {
            Rate rate = defaultRate(RATE_DATE, BigDecimal.valueOf(100_000));

            assertThat(rate.id().isNew()).isTrue();
            assertThat(rate.ratePlanId()).isEqualTo(RATE_PLAN_ID);
            assertThat(rate.rateDate()).isEqualTo(RATE_DATE);
            assertThat(rate.basePrice()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
            assertThat(rate.createdAt()).isEqualTo(NOW);
            assertThat(rate.updatedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("rateDate가 null이면 예외를 던진다")
        void shouldFailWhenRateDateIsNull() {
            assertThatThrownBy(() -> Rate.forNew(RATE_PLAN_ID, null, BigDecimal.valueOf(100_000), NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("요금 날짜는 필수");
        }

        @Test
        @DisplayName("basePrice가 null이면 예외를 던진다")
        void shouldFailWhenBasePriceIsNull() {
            assertThatThrownBy(() -> Rate.forNew(RATE_PLAN_ID, RATE_DATE, null, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("기본 가격은 0 이상");
        }

        @Test
        @DisplayName("basePrice가 음수이면 예외를 던진다")
        void shouldFailWhenBasePriceIsNegative() {
            assertThatThrownBy(() -> Rate.forNew(RATE_PLAN_ID, RATE_DATE, BigDecimal.valueOf(-1), NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("기본 가격은 0 이상");
        }

        @Test
        @DisplayName("basePrice가 0이면 허용된다")
        void shouldAllowZeroBasePrice() {
            Rate rate = Rate.forNew(RATE_PLAN_ID, RATE_DATE, BigDecimal.ZERO, NOW);
            assertThat(rate.basePrice()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("T-2: updatePrice() 가격 변경")
    class UpdatePrice {

        @Test
        @DisplayName("정상적으로 가격을 변경한다")
        void shouldUpdatePriceSuccessfully() {
            Rate rate = defaultRate(RATE_DATE, BigDecimal.valueOf(100_000));

            rate.updatePrice(BigDecimal.valueOf(120_000), LATER);

            assertThat(rate.basePrice()).isEqualByComparingTo(BigDecimal.valueOf(120_000));
            assertThat(rate.updatedAt()).isEqualTo(LATER);
        }

        @Test
        @DisplayName("null 가격으로 변경 시 예외를 던진다")
        void shouldFailWhenNewPriceIsNull() {
            Rate rate = defaultRate(RATE_DATE, BigDecimal.valueOf(100_000));

            assertThatThrownBy(() -> rate.updatePrice(null, LATER))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("가격은 0 이상");
        }

        @Test
        @DisplayName("음수 가격으로 변경 시 예외를 던진다")
        void shouldFailWhenNewPriceIsNegative() {
            Rate rate = defaultRate(RATE_DATE, BigDecimal.valueOf(100_000));

            assertThatThrownBy(() -> rate.updatePrice(BigDecimal.valueOf(-1), LATER))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("가격은 0 이상");
        }

        @Test
        @DisplayName("0으로 변경은 허용된다")
        void shouldAllowUpdateToZero() {
            Rate rate = defaultRate(RATE_DATE, BigDecimal.valueOf(100_000));

            rate.updatePrice(BigDecimal.ZERO, LATER);

            assertThat(rate.basePrice()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("T-3: reconstitute() 복원")
    class Reconstitute {

        @Test
        @DisplayName("검증 없이 모든 필드를 복원한다")
        void shouldReconstituteFaithfully() {
            Rate rate = reconstitutedRate(42L, RATE_DATE, BigDecimal.valueOf(100_000));

            assertThat(rate.id()).isEqualTo(RateId.of(42L));
            assertThat(rate.ratePlanId()).isEqualTo(RATE_PLAN_ID);
            assertThat(rate.rateDate()).isEqualTo(RATE_DATE);
            assertThat(rate.basePrice()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
        }
    }

    @Nested
    @DisplayName("T-6: equals/hashCode")
    class EqualsHashCode {

        @Test
        @DisplayName("같은 ID의 Rate는 동등하다")
        void shouldBeEqualWithSameId() {
            Rate a = reconstitutedRate(1L, RATE_DATE, BigDecimal.valueOf(100_000));
            Rate b = reconstitutedRate(1L, RATE_DATE, BigDecimal.valueOf(200_000));

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("다른 ID의 Rate는 동등하지 않다")
        void shouldNotBeEqualWithDifferentId() {
            Rate a = reconstitutedRate(1L, RATE_DATE, BigDecimal.valueOf(100_000));
            Rate b = reconstitutedRate(2L, RATE_DATE, BigDecimal.valueOf(100_000));

            assertThat(a).isNotEqualTo(b);
        }
    }
}
