package com.ryuqq.otatoy.application.pricing.manager;

import com.ryuqq.otatoy.application.pricing.port.out.RateQueryPort;
import com.ryuqq.otatoy.domain.pricing.PricingFixtures;
import com.ryuqq.otatoy.domain.pricing.Rate;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * RateReadManager 단위 테스트.
 * Port 위임 로직을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class RateReadManagerTest {

    @Mock RateQueryPort rateQueryPort;
    @InjectMocks RateReadManager manager;

    private static final RatePlanId PLAN_ID = RatePlanId.of(1L);
    private static final LocalDate START = LocalDate.of(2026, 4, 1);
    private static final LocalDate END = LocalDate.of(2026, 4, 10);

    @Nested
    @DisplayName("findByRatePlanIdsAndDateRange")
    class FindByRatePlanIdsAndDateRange {

        @Test
        @DisplayName("Port에 위임하고 결과를 그대로 반환한다")
        void shouldDelegateToPortAndReturnResults() {
            // given
            List<RatePlanId> planIds = List.of(PLAN_ID);
            Rate rate = PricingFixtures.reconstitutedRate(1L, START, BigDecimal.valueOf(100_000));
            given(rateQueryPort.findByRatePlanIdsAndDateRange(planIds, START, END)).willReturn(List.of(rate));

            // when
            List<Rate> result = manager.findByRatePlanIdsAndDateRange(planIds, START, END);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isSameAs(rate);
        }
    }

    @Nested
    @DisplayName("findByRatePlanIdAndDateRange")
    class FindByRatePlanIdAndDateRange {

        @Test
        @DisplayName("단일 RatePlanId를 List로 감싸서 Port에 위임한다")
        void shouldWrapSingleIdAndDelegate() {
            // given
            Rate rate = PricingFixtures.reconstitutedRate(1L, START, BigDecimal.valueOf(100_000));
            given(rateQueryPort.findByRatePlanIdsAndDateRange(List.of(PLAN_ID), START, END))
                .willReturn(List.of(rate));

            // when
            List<Rate> result = manager.findByRatePlanIdAndDateRange(PLAN_ID, START, END);

            // then
            assertThat(result).hasSize(1);
            then(rateQueryPort).should().findByRatePlanIdsAndDateRange(List.of(PLAN_ID), START, END);
        }
    }
}
