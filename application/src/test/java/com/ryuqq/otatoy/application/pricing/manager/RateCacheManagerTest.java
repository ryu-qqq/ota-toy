package com.ryuqq.otatoy.application.pricing.manager;

import com.ryuqq.otatoy.application.pricing.port.out.RateCachePort;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * RateCacheManager 단위 테스트.
 * Redis 캐시 히트/미스 시나리오와 DB 폴백 + 캐시 적재 로직을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class RateCacheManagerTest {

    @Mock RateCachePort rateCachePort;
    @Mock RateReadManager rateReadManager;

    @InjectMocks RateCacheManager rateCacheManager;

    private static final RatePlanId PLAN_1 = RatePlanId.of(1L);
    private static final LocalDate DATE_1 = LocalDate.of(2026, 4, 10);
    private static final LocalDate DATE_2 = LocalDate.of(2026, 4, 11);

    @Nested
    @DisplayName("캐시 전체 히트")
    class CacheFullHit {

        @Test
        @DisplayName("모든 키가 캐시에 있으면 DB를 조회하지 않는다")
        void shouldNotCallDbWhenAllCached() {
            // given
            List<RatePlanId> planIds = List.of(PLAN_1);
            List<LocalDate> dates = List.of(DATE_1, DATE_2);

            Map<String, BigDecimal> cached = new HashMap<>();
            cached.put("1:2026-04-10", BigDecimal.valueOf(100_000));
            cached.put("1:2026-04-11", BigDecimal.valueOf(120_000));

            given(rateCachePort.multiGet(List.of(1L), dates)).willReturn(cached);

            // when
            Map<String, BigDecimal> result = rateCacheManager.getRates(planIds, dates);

            // then
            assertThat(result).hasSize(2);
            then(rateReadManager).shouldHaveNoInteractions();
            then(rateCachePort).should(never()).multiSet(any());
        }
    }

    @Nested
    @DisplayName("캐시 미스 - DB 폴백")
    class CacheMiss {

        @Test
        @DisplayName("캐시 미스분만 DB에서 조회하고 캐시에 적재한다")
        void shouldLoadMissedFromDbAndCache() {
            // given
            List<RatePlanId> planIds = List.of(PLAN_1);
            List<LocalDate> dates = List.of(DATE_1, DATE_2);

            // 4/10은 캐시 히트, 4/11은 미스
            Map<String, BigDecimal> cached = new HashMap<>();
            cached.put("1:2026-04-10", BigDecimal.valueOf(100_000));

            given(rateCachePort.multiGet(List.of(1L), dates)).willReturn(cached);

            // DB에서 4/11 조회
            Rate rateFromDb = PricingFixtures.reconstitutedRate(1L, DATE_2, BigDecimal.valueOf(120_000));
            given(rateReadManager.findByRatePlanIdsAndDateRange(anyList(), any(), any()))
                .willReturn(List.of(rateFromDb));

            // when
            Map<String, BigDecimal> result = rateCacheManager.getRates(planIds, dates);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get("1:2026-04-11")).isEqualByComparingTo(BigDecimal.valueOf(120_000));
            then(rateCachePort).should().multiSet(any());
        }
    }

    @Nested
    @DisplayName("캐시 전체 미스")
    class CacheFullMiss {

        @Test
        @DisplayName("캐시가 완전히 비어있으면 모든 데이터를 DB에서 조회한다")
        void shouldLoadAllFromDbWhenCacheEmpty() {
            // given
            List<RatePlanId> planIds = List.of(PLAN_1);
            List<LocalDate> dates = List.of(DATE_1, DATE_2);

            Map<String, BigDecimal> emptyCached = new HashMap<>();
            given(rateCachePort.multiGet(List.of(1L), dates)).willReturn(emptyCached);

            Rate rate1 = PricingFixtures.reconstitutedRate(1L, DATE_1, BigDecimal.valueOf(100_000));
            Rate rate2 = PricingFixtures.reconstitutedRate(2L, DATE_2, BigDecimal.valueOf(120_000));
            given(rateReadManager.findByRatePlanIdsAndDateRange(anyList(), any(), any()))
                .willReturn(List.of(rate1, rate2));

            // when
            Map<String, BigDecimal> result = rateCacheManager.getRates(planIds, dates);

            // then
            assertThat(result).hasSize(2);
            then(rateCachePort).should().multiSet(any());
        }
    }

    @Nested
    @DisplayName("DB 조회 결과 없음")
    class DbEmpty {

        @Test
        @DisplayName("DB에도 데이터가 없으면 캐시 적재를 하지 않는다")
        void shouldNotCacheWhenDbReturnsEmpty() {
            // given
            List<RatePlanId> planIds = List.of(PLAN_1);
            List<LocalDate> dates = List.of(DATE_1);

            Map<String, BigDecimal> emptyCached = new HashMap<>();
            given(rateCachePort.multiGet(List.of(1L), dates)).willReturn(emptyCached);
            given(rateReadManager.findByRatePlanIdsAndDateRange(anyList(), any(), any()))
                .willReturn(List.of());

            // when
            Map<String, BigDecimal> result = rateCacheManager.getRates(planIds, dates);

            // then
            assertThat(result).isEmpty();
            then(rateCachePort).should(never()).multiSet(any());
        }
    }
}
