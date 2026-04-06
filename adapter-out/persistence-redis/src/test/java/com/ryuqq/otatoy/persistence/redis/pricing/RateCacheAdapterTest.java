package com.ryuqq.otatoy.persistence.redis.pricing;

import com.ryuqq.otatoy.persistence.redis.RedisTestContainerConfig;
import com.ryuqq.otatoy.persistence.redis.adapter.RateCacheAdapter;
import com.ryuqq.otatoy.persistence.redis.config.RateCacheProperties;
import com.ryuqq.otatoy.persistence.redis.support.RateCacheKeyResolver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Rate 캐시 Redis Adapter 통합 테스트.
 * Testcontainers Redis + Redisson Batch(MGET/MSET)를 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
class RateCacheAdapterTest extends RedisTestContainerConfig {

    private static RedissonClient redissonClient;
    private RateCacheAdapter adapter;
    private RateCacheKeyResolver keyResolver;
    private RateCacheProperties properties;

    private final Long ratePlanId1 = 10L;
    private final Long ratePlanId2 = 20L;
    private final LocalDate date1 = LocalDate.of(2026, 7, 1);
    private final LocalDate date2 = LocalDate.of(2026, 7, 2);

    @BeforeAll
    static void setUpRedis() {
        redissonClient = createRedissonClient();
    }

    @AfterAll
    static void tearDown() {
        if (redissonClient != null) {
            redissonClient.shutdown();
        }
    }

    @BeforeEach
    void setUp() {
        properties = new RateCacheProperties();
        properties.setKeyPrefix("test-rate:");
        properties.setTtl(Duration.ofHours(1));
        keyResolver = new RateCacheKeyResolver(properties);
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        adapter = new RateCacheAdapter(redissonClient, keyResolver, properties, meterRegistry);

        // 테스트 격리: 기존 키 정리
        List<String> keys = keyResolver.resolveAll(List.of(ratePlanId1, ratePlanId2), List.of(date1, date2));
        for (String key : keys) {
            redissonClient.getBucket(key).delete();
        }
    }

    @Nested
    @DisplayName("multiSet + multiGet — 저장/조회 왕복")
    class SetAndGetTest {

        @Test
        @DisplayName("저장한 요금을 정확히 조회할 수 있다")
        void shouldSetAndGetRates() {
            // given
            String key1 = keyResolver.resultKey(ratePlanId1, date1);
            String key2 = keyResolver.resultKey(ratePlanId1, date2);
            Map<String, BigDecimal> rates = Map.of(
                key1, new BigDecimal("150000"),
                key2, new BigDecimal("180000")
            );

            // when
            adapter.multiSet(rates);
            Map<String, BigDecimal> result = adapter.multiGet(List.of(ratePlanId1), List.of(date1, date2));

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(key1)).isEqualByComparingTo(new BigDecimal("150000"));
            assertThat(result.get(key2)).isEqualByComparingTo(new BigDecimal("180000"));
        }

        @Test
        @DisplayName("다중 ratePlanId × 날짜 조합을 저장/조회할 수 있다")
        void shouldHandleMultipleRatePlans() {
            // given
            String key1 = keyResolver.resultKey(ratePlanId1, date1);
            String key2 = keyResolver.resultKey(ratePlanId2, date1);
            Map<String, BigDecimal> rates = Map.of(
                key1, new BigDecimal("100000"),
                key2, new BigDecimal("200000")
            );

            // when
            adapter.multiSet(rates);
            Map<String, BigDecimal> result = adapter.multiGet(
                List.of(ratePlanId1, ratePlanId2), List.of(date1));

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(key1)).isEqualByComparingTo(new BigDecimal("100000"));
            assertThat(result.get(key2)).isEqualByComparingTo(new BigDecimal("200000"));
        }
    }

    @Nested
    @DisplayName("multiGet — 미존재 키 처리")
    class CacheMissTest {

        @Test
        @DisplayName("캐시에 없는 키는 결과 Map에 포함되지 않는다")
        void shouldExcludeNonExistentKeys() {
            // given: 아무것도 저장하지 않음

            // when
            Map<String, BigDecimal> result = adapter.multiGet(List.of(ratePlanId1), List.of(date1));

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("일부만 캐시에 있으면 존재하는 것만 반환한다")
        void shouldReturnOnlyCachedEntries() {
            // given: date1만 저장
            String key1 = keyResolver.resultKey(ratePlanId1, date1);
            adapter.multiSet(Map.of(key1, new BigDecimal("100000")));

            // when: date1, date2 둘 다 조회
            Map<String, BigDecimal> result = adapter.multiGet(List.of(ratePlanId1), List.of(date1, date2));

            // then
            assertThat(result).hasSize(1);
            assertThat(result).containsKey(key1);
            assertThat(result).doesNotContainKey(keyResolver.resultKey(ratePlanId1, date2));
        }
    }

    @Nested
    @DisplayName("빈 입력 처리")
    class EmptyInputTest {

        @Test
        @DisplayName("빈 ratePlanIds로 multiGet 호출 시 빈 Map을 반환한다")
        void shouldReturnEmptyForEmptyRatePlanIds() {
            Map<String, BigDecimal> result = adapter.multiGet(List.of(), List.of(date1));
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("빈 dates로 multiGet 호출 시 빈 Map을 반환한다")
        void shouldReturnEmptyForEmptyDates() {
            Map<String, BigDecimal> result = adapter.multiGet(List.of(ratePlanId1), List.of());
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null 입력으로 multiGet 호출 시 빈 Map을 반환한다")
        void shouldReturnEmptyForNullInput() {
            Map<String, BigDecimal> result = adapter.multiGet(null, null);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("빈 Map으로 multiSet 호출 시 예외 없이 무시한다")
        void shouldIgnoreEmptyMultiSet() {
            adapter.multiSet(Map.of());
            // 예외 없이 통과하면 성공
        }

        @Test
        @DisplayName("null Map으로 multiSet 호출 시 예외 없이 무시한다")
        void shouldIgnoreNullMultiSet() {
            adapter.multiSet(null);
            // 예외 없이 통과하면 성공
        }
    }

    @Nested
    @DisplayName("BigDecimal 정밀도")
    class PrecisionTest {

        @Test
        @DisplayName("소수점 가격이 정확하게 저장/복원된다")
        void shouldPreserveDecimalPrecision() {
            // given
            String key = keyResolver.resultKey(ratePlanId1, date1);
            BigDecimal price = new BigDecimal("99999.99");

            // when
            adapter.multiSet(Map.of(key, price));
            Map<String, BigDecimal> result = adapter.multiGet(List.of(ratePlanId1), List.of(date1));

            // then
            assertThat(result.get(key)).isEqualByComparingTo(price);
        }

        @Test
        @DisplayName("0원 가격이 정상적으로 저장/복원된다")
        void shouldHandleZeroPrice() {
            // given
            String key = keyResolver.resultKey(ratePlanId1, date1);

            // when
            adapter.multiSet(Map.of(key, BigDecimal.ZERO));
            Map<String, BigDecimal> result = adapter.multiGet(List.of(ratePlanId1), List.of(date1));

            // then
            assertThat(result.get(key)).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("TTL 검증")
    class TtlTest {

        @Test
        @DisplayName("저장된 요금에 TTL이 설정된다")
        void shouldSetTtlOnCachedRates() {
            // given
            String key = keyResolver.resultKey(ratePlanId1, date1);
            adapter.multiSet(Map.of(key, new BigDecimal("100000")));

            // when
            String redisKey = properties.getKeyPrefix() + key;
            long ttl = redissonClient.getBucket(redisKey).remainTimeToLive();

            // then: TTL이 0보다 크고, 설정값(1시간=3600000ms) 이하
            assertThat(ttl).isGreaterThan(0);
            assertThat(ttl).isLessThanOrEqualTo(properties.getTtlMillis());
        }
    }
}
