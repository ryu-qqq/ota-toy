package com.ryuqq.otatoy.persistence.redis.inventory;

import com.ryuqq.otatoy.domain.inventory.InventoryExhaustedException;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.persistence.redis.RedisTestContainerConfig;
import com.ryuqq.otatoy.persistence.redis.adapter.InventoryRedisAdapter;
import com.ryuqq.otatoy.persistence.redis.support.InventoryKeyResolver;
import com.ryuqq.otatoy.persistence.redis.support.InventoryLuaScriptHolder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Inventory Redis 동시성 테스트.
 * Lua 스크립트의 원자적 차감/롤백을 멀티 스레드로 검증한다.
 * 과제 핵심 요구사항: "동일한 재고에 대해 동시 예약 요청이 발생할 수 있는 상황을 처리해야 한다."
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
class InventoryRedisConcurrencyTest extends RedisTestContainerConfig {

    private static RedissonClient redissonClient;
    private InventoryRedisAdapter adapter;

    private final RoomTypeId roomTypeId = RoomTypeId.of(200L);
    private final LocalDate date1 = LocalDate.of(2026, 6, 1);
    private final LocalDate date2 = LocalDate.of(2026, 6, 2);

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
        adapter = new InventoryRedisAdapter(redissonClient, new InventoryLuaScriptHolder());

        // 테스트 격리
        List<String> keys = InventoryKeyResolver.resolveAll(roomTypeId, List.of(date1, date2));
        for (String key : keys) {
            redissonClient.getBucket(key, StringCodec.INSTANCE).delete();
        }
    }

    @Nested
    @DisplayName("동시 예약 — 재고 1개, 10 동시 요청")
    class SingleStockConcurrencyTest {

        @Test
        @DisplayName("재고 1개에 10개 동시 차감 요청 시 정확히 1개만 성공한다")
        void shouldAllowExactlyOneSuccessForSingleStock() throws InterruptedException {
            // given
            adapter.initializeStock(roomTypeId, Map.of(date1, 1, date2, 1));

            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch ready = new CountDownLatch(threadCount);
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threadCount);

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);

            // when
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        ready.countDown();
                        start.await(); // 모든 스레드 동시 출발
                        adapter.decrementStock(roomTypeId, List.of(date1, date2));
                        successCount.incrementAndGet();
                    } catch (InventoryExhaustedException e) {
                        failCount.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }

            ready.await();
            start.countDown(); // 동시 출발
            done.await();
            executor.shutdown();

            // then
            assertThat(successCount.get()).isEqualTo(1);
            assertThat(failCount.get()).isEqualTo(9);

            // 최종 재고: 0
            assertThat(getValue(date1)).isEqualTo("0");
            assertThat(getValue(date2)).isEqualTo("0");
        }
    }

    @Nested
    @DisplayName("동시 예약 — 재고 5개, 10 동시 요청")
    class MultiStockConcurrencyTest {

        @Test
        @DisplayName("재고 5개에 10개 동시 차감 요청 시 정확히 5개만 성공한다")
        void shouldAllowExactlyFiveSuccessesForFiveStock() throws InterruptedException {
            // given
            adapter.initializeStock(roomTypeId, Map.of(date1, 5, date2, 5));

            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch ready = new CountDownLatch(threadCount);
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threadCount);

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);

            // when
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        ready.countDown();
                        start.await();
                        adapter.decrementStock(roomTypeId, List.of(date1, date2));
                        successCount.incrementAndGet();
                    } catch (InventoryExhaustedException e) {
                        failCount.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }

            ready.await();
            start.countDown();
            done.await();
            executor.shutdown();

            // then
            assertThat(successCount.get()).isEqualTo(5);
            assertThat(failCount.get()).isEqualTo(5);

            assertThat(getValue(date1)).isEqualTo("0");
            assertThat(getValue(date2)).isEqualTo("0");
        }
    }

    @Nested
    @DisplayName("동시 차감 + 복구 혼합")
    class MixedConcurrencyTest {

        @Test
        @DisplayName("차감과 복구가 동시에 실행되어도 최종 재고가 정합성을 유지한다")
        void shouldMaintainConsistencyWithMixedOperations() throws InterruptedException {
            // given: 재고 10
            adapter.initializeStock(roomTypeId, Map.of(date1, 10));

            int decrementCount = 5;
            int incrementCount = 3;
            ExecutorService executor = Executors.newFixedThreadPool(decrementCount + incrementCount);
            CountDownLatch done = new CountDownLatch(decrementCount + incrementCount);

            // when: 5번 차감 + 3번 복구 = 최종 10 - 5 + 3 = 8
            for (int i = 0; i < decrementCount; i++) {
                executor.submit(() -> {
                    try {
                        adapter.decrementStock(roomTypeId, List.of(date1));
                    } finally {
                        done.countDown();
                    }
                });
            }
            for (int i = 0; i < incrementCount; i++) {
                executor.submit(() -> {
                    try {
                        adapter.incrementStock(roomTypeId, List.of(date1));
                    } finally {
                        done.countDown();
                    }
                });
            }

            done.await();
            executor.shutdown();

            // then
            assertThat(getValue(date1)).isEqualTo("8");
        }
    }

    private String getValue(LocalDate date) {
        String key = InventoryKeyResolver.resolve(roomTypeId, date);
        Object value = redissonClient.getBucket(key, StringCodec.INSTANCE).get();
        return value != null ? value.toString() : null;
    }
}
