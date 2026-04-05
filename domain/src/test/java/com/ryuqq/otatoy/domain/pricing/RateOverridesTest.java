package com.ryuqq.otatoy.domain.pricing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.ryuqq.otatoy.domain.pricing.PricingFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RateOverridesTest {

    @Nested
    @DisplayName("T-1: forNew() 생성 검증")
    class Creation {

        @Test
        @DisplayName("날짜가 겹치지 않는 오버라이드들은 성공한다")
        void shouldCreateWhenNoDuplicate() {
            RateOverrides overrides = RateOverrides.forNew(nonDuplicateOverrides());

            assertThat(overrides.size()).isEqualTo(2);
            assertThat(overrides.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("빈 리스트는 성공한다")
        void shouldCreateWithEmptyList() {
            RateOverrides overrides = RateOverrides.forNew(List.of());

            assertThat(overrides.isEmpty()).isTrue();
            assertThat(overrides.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("null 리스트는 빈 컬렉션으로 생성된다")
        void shouldCreateWithNull() {
            RateOverrides overrides = RateOverrides.forNew(null);

            assertThat(overrides.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("단일 항목은 성공한다")
        void shouldCreateWithSingleItem() {
            RateOverrides overrides = RateOverrides.forNew(List.of(defaultOverride()));

            assertThat(overrides.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("같은 날짜의 오버라이드가 있으면 실패한다")
        void shouldFailWhenDuplicateDate() {
            assertThatThrownBy(() -> RateOverrides.forNew(duplicateDateOverrides()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("오버라이드 날짜가 중복됩니다");
        }
    }

    @Nested
    @DisplayName("T-3: reconstitute — DB 복원")
    class Reconstitute {

        @Test
        @DisplayName("중복 검증 없이 복원된다")
        void shouldReconstituteWithoutValidation() {
            RateOverrides overrides = RateOverrides.reconstitute(duplicateDateOverrides());

            assertThat(overrides.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("null 리스트는 빈 컬렉션으로 복원된다")
        void shouldReconstituteWithNull() {
            RateOverrides overrides = RateOverrides.reconstitute(null);

            assertThat(overrides.isEmpty()).isTrue();
        }
    }
}
