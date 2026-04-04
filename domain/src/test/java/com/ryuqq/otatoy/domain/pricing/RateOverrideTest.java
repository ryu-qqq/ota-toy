package com.ryuqq.otatoy.domain.pricing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.ryuqq.otatoy.domain.pricing.PricingFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RateOverrideTest {

    @Nested
    @DisplayName("T-1: forNew() 생성 검증")
    class Creation {

        @Test
        @DisplayName("정상 생성 시 모든 필드가 올바르게 설정된다")
        void shouldCreateRateOverrideSuccessfully() {
            RateOverride override = defaultOverride();

            assertThat(override.id().isNew()).isTrue();
            assertThat(override.rateRuleId()).isEqualTo(RATE_RULE_ID);
            assertThat(override.overrideDate()).isEqualTo(LocalDate.of(2026, 4, 5));
            assertThat(override.price()).isEqualByComparingTo(BigDecimal.valueOf(170_000));
            assertThat(override.reason()).isEqualTo("공휴일 특가");
            assertThat(override.createdAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("overrideDate가 null이면 예외를 던진다")
        void shouldFailWhenOverrideDateIsNull() {
            assertThatThrownBy(() -> RateOverride.forNew(
                    RATE_RULE_ID, RULE_START_DATE, RULE_END_DATE,
                    null, BigDecimal.valueOf(100_000), "테스트", NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("오버라이드 날짜는 필수");
        }

        @Test
        @DisplayName("price가 null이면 예외를 던진다")
        void shouldFailWhenPriceIsNull() {
            assertThatThrownBy(() -> RateOverride.forNew(
                    RATE_RULE_ID, RULE_START_DATE, RULE_END_DATE,
                    LocalDate.of(2026, 4, 5), null, "테스트", NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("가격은 0 이상");
        }

        @Test
        @DisplayName("price가 음수이면 예외를 던진다")
        void shouldFailWhenPriceIsNegative() {
            assertThatThrownBy(() -> RateOverride.forNew(
                    RATE_RULE_ID, RULE_START_DATE, RULE_END_DATE,
                    LocalDate.of(2026, 4, 5), BigDecimal.valueOf(-1), "테스트", NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("가격은 0 이상");
        }

        @Test
        @DisplayName("price가 0이면 허용된다 (무료 프로모션)")
        void shouldAllowZeroPrice() {
            RateOverride override = RateOverride.forNew(
                    RATE_RULE_ID, RULE_START_DATE, RULE_END_DATE,
                    LocalDate.of(2026, 4, 5),
                    BigDecimal.ZERO, "무료 프로모션", NOW
            );

            assertThat(override.price()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("reason이 null이어도 허용된다")
        void shouldAllowNullReason() {
            RateOverride override = RateOverride.forNew(
                    RATE_RULE_ID, RULE_START_DATE, RULE_END_DATE,
                    LocalDate.of(2026, 4, 5),
                    BigDecimal.valueOf(100_000), null, NOW
            );

            assertThat(override.reason()).isNull();
        }

        @Test
        @DisplayName("overrideDate가 요금 규칙 시작일 이전이면 예외를 던진다")
        void shouldFailWhenOverrideDateBeforeRuleStart() {
            assertThatThrownBy(() -> RateOverride.forNew(
                    RATE_RULE_ID, RULE_START_DATE, RULE_END_DATE,
                    LocalDate.of(2026, 3, 31), BigDecimal.valueOf(100_000), "범위 밖", NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("요금 규칙 범위 내여야 합니다");
        }

        @Test
        @DisplayName("overrideDate가 요금 규칙 종료일 이후이면 예외를 던진다")
        void shouldFailWhenOverrideDateAfterRuleEnd() {
            assertThatThrownBy(() -> RateOverride.forNew(
                    RATE_RULE_ID, RULE_START_DATE, RULE_END_DATE,
                    LocalDate.of(2026, 5, 1), BigDecimal.valueOf(100_000), "범위 밖", NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("요금 규칙 범위 내여야 합니다");
        }

        @Test
        @DisplayName("overrideDate가 요금 규칙 경계값(시작일, 종료일)이면 허용된다")
        void shouldAllowOverrideDateOnBoundary() {
            RateOverride startOverride = RateOverride.forNew(
                    RATE_RULE_ID, RULE_START_DATE, RULE_END_DATE,
                    RULE_START_DATE, BigDecimal.valueOf(100_000), "시작일", NOW
            );
            RateOverride endOverride = RateOverride.forNew(
                    RATE_RULE_ID, RULE_START_DATE, RULE_END_DATE,
                    RULE_END_DATE, BigDecimal.valueOf(100_000), "종료일", NOW
            );

            assertThat(startOverride.overrideDate()).isEqualTo(RULE_START_DATE);
            assertThat(endOverride.overrideDate()).isEqualTo(RULE_END_DATE);
        }
    }

    @Nested
    @DisplayName("T-3: reconstitute() 복원")
    class Reconstitute {

        @Test
        @DisplayName("검증 없이 모든 필드를 복원한다")
        void shouldReconstituteFaithfully() {
            RateOverride override = reconstitutedOverride(42L);

            assertThat(override.id()).isEqualTo(RateOverrideId.of(42L));
            assertThat(override.rateRuleId()).isEqualTo(RATE_RULE_ID);
            assertThat(override.overrideDate()).isEqualTo(LocalDate.of(2026, 4, 5));
            assertThat(override.price()).isEqualByComparingTo(BigDecimal.valueOf(170_000));
            assertThat(override.reason()).isEqualTo("공휴일 특가");
        }
    }

    @Nested
    @DisplayName("T-6: equals/hashCode")
    class EqualsHashCode {

        @Test
        @DisplayName("같은 ID의 RateOverride는 동등하다")
        void shouldBeEqualWithSameId() {
            RateOverride a = reconstitutedOverride(1L);
            RateOverride b = reconstitutedOverride(1L);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("다른 ID의 RateOverride는 동등하지 않다")
        void shouldNotBeEqualWithDifferentId() {
            RateOverride a = reconstitutedOverride(1L);
            RateOverride b = reconstitutedOverride(2L);

            assertThat(a).isNotEqualTo(b);
        }
    }
}
