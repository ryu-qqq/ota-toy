package com.ryuqq.otatoy.domain.pricing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.ryuqq.otatoy.domain.pricing.PricingFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RateRulesTest {

    @Nested
    @DisplayName("T-1: forNew() 생성 검증")
    class Creation {

        @Test
        @DisplayName("겹치지 않는 규칙들은 성공한다")
        void shouldCreateWhenNoOverlap() {
            RateRules rules = RateRules.forNew(nonOverlappingRateRules());

            assertThat(rules.size()).isEqualTo(2);
            assertThat(rules.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("빈 리스트는 성공한다")
        void shouldCreateWithEmptyList() {
            RateRules rules = RateRules.forNew(List.of());

            assertThat(rules.isEmpty()).isTrue();
            assertThat(rules.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("null 리스트는 빈 컬렉션으로 생성된다")
        void shouldCreateWithNull() {
            RateRules rules = RateRules.forNew(null);

            assertThat(rules.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("단일 항목은 성공한다")
        void shouldCreateWithSingleItem() {
            RateRules rules = RateRules.forNew(List.of(defaultRateRule()));

            assertThat(rules.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("완전히 겹치는 규칙들은 실패한다")
        void shouldFailWhenFullyOverlapping() {
            assertThatThrownBy(() -> RateRules.forNew(fullyOverlappingRateRules()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("요금 규칙 기간이 겹칩니다");
        }

        @Test
        @DisplayName("부분 겹침이 있으면 실패한다")
        void shouldFailWhenPartiallyOverlapping() {
            assertThatThrownBy(() -> RateRules.forNew(partiallyOverlappingRateRules()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("요금 규칙 기간이 겹칩니다");
        }

        @Test
        @DisplayName("인접하지만 겹치지 않는 규칙들은 성공한다")
        void shouldCreateWhenAdjacentButNotOverlapping() {
            RateRules rules = RateRules.forNew(adjacentRateRules());

            assertThat(rules.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("T-3: reconstitute — DB 복원")
    class Reconstitute {

        @Test
        @DisplayName("겹침 검증 없이 복원된다")
        void shouldReconstituteWithoutValidation() {
            RateRules rules = RateRules.reconstitute(fullyOverlappingRateRules());

            assertThat(rules.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("null 리스트는 빈 컬렉션으로 복원된다")
        void shouldReconstituteWithNull() {
            RateRules rules = RateRules.reconstitute(null);

            assertThat(rules.isEmpty()).isTrue();
        }
    }
}
