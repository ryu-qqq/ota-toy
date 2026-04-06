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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Inventory Redis Adapter 통합 테스트.
 * Testcontainers Redis + Lua 스크립트 기반 원자적 재고 연산을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
class InventoryRedisAdapterTest extends RedisTestContainerConfig {

    private static RedissonClient redissonClient;
    private InventoryRedisAdapter adapter;

    private final RoomTypeId roomTypeId = RoomTypeId.of(100L);
    private final LocalDate date1 = LocalDate.of(2026, 5, 1);
    private final LocalDate date2 = LocalDate.of(2026, 5, 2);
    private final LocalDate date3 = LocalDate.of(2026, 5, 3);

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

        // 테스트 격리: 기존 키 정리
        List<String> keys = InventoryKeyResolver.resolveAll(roomTypeId, List.of(date1, date2, date3));
        for (String key : keys) {
            redissonClient.getBucket(key, StringCodec.INSTANCE).delete();
        }
    }

    @Nested
    @DisplayName("initializeStock — 재고 초기화")
    class InitializeStockTest {

        @Test
        @DisplayName("다중 날짜 재고를 초기화하면 각 키에 값이 설정된다")
        void shouldInitializeMultipleDateStocks() {
            // when
            adapter.initializeStock(roomTypeId, Map.of(date1, 10, date2, 5, date3, 3));

            // then
            assertThat(getValue(roomTypeId, date1)).isEqualTo("10");
            assertThat(getValue(roomTypeId, date2)).isEqualTo("5");
            assertThat(getValue(roomTypeId, date3)).isEqualTo("3");
        }

        @Test
        @DisplayName("이미 존재하는 키에 초기화하면 값이 덮어쓰기된다")
        void shouldOverwriteExistingStock() {
            // given
            adapter.initializeStock(roomTypeId, Map.of(date1, 10));

            // when
            adapter.initializeStock(roomTypeId, Map.of(date1, 20));

            // then
            assertThat(getValue(roomTypeId, date1)).isEqualTo("20");
        }
    }

    @Nested
    @DisplayName("decrementStock — 재고 차감")
    class DecrementStockTest {

        @Test
        @DisplayName("재고가 충분하면 모든 날짜에서 1씩 차감된다")
        void shouldDecrementWhenStockSufficient() {
            // given
            adapter.initializeStock(roomTypeId, Map.of(date1, 5, date2, 3));

            // when
            adapter.decrementStock(roomTypeId, List.of(date1, date2));

            // then
            assertThat(getValue(roomTypeId, date1)).isEqualTo("4");
            assertThat(getValue(roomTypeId, date2)).isEqualTo("2");
        }

        @Test
        @DisplayName("재고가 0인 날짜가 있으면 InventoryExhaustedException이 발생하고 전체 롤백된다")
        void shouldThrowAndRollbackWhenAnyDateExhausted() {
            // given: date1=1, date2=0
            adapter.initializeStock(roomTypeId, Map.of(date1, 1, date2, 0));

            // when & then
            assertThatThrownBy(() -> adapter.decrementStock(roomTypeId, List.of(date1, date2)))
                .isInstanceOf(InventoryExhaustedException.class);

            // 롤백 확인: date1도 원래 값 유지
            assertThat(getValue(roomTypeId, date1)).isEqualTo("1");
            assertThat(getValue(roomTypeId, date2)).isEqualTo("0");
        }

        @Test
        @DisplayName("재고 1에서 차감 후 0이 되면 정상 성공한다")
        void shouldSucceedWhenStockBecomesZero() {
            // given
            adapter.initializeStock(roomTypeId, Map.of(date1, 1));

            // when
            adapter.decrementStock(roomTypeId, List.of(date1));

            // then
            assertThat(getValue(roomTypeId, date1)).isEqualTo("0");
        }

        @Test
        @DisplayName("연속 차감 시 재고가 0이 되면 다음 차감에서 예외가 발생한다")
        void shouldFailOnSecondDecrementWhenStockBecomesZero() {
            // given
            adapter.initializeStock(roomTypeId, Map.of(date1, 1));
            adapter.decrementStock(roomTypeId, List.of(date1)); // 1 → 0

            // when & then
            assertThatThrownBy(() -> adapter.decrementStock(roomTypeId, List.of(date1)))
                .isInstanceOf(InventoryExhaustedException.class);

            // 롤백 확인
            assertThat(getValue(roomTypeId, date1)).isEqualTo("0");
        }
    }

    @Nested
    @DisplayName("incrementStock — 재고 복구")
    class IncrementStockTest {

        @Test
        @DisplayName("차감 후 복구하면 원래 재고로 돌아온다")
        void shouldRestoreStockAfterDecrement() {
            // given
            adapter.initializeStock(roomTypeId, Map.of(date1, 5, date2, 3));
            adapter.decrementStock(roomTypeId, List.of(date1, date2)); // 4, 2

            // when
            adapter.incrementStock(roomTypeId, List.of(date1, date2));

            // then
            assertThat(getValue(roomTypeId, date1)).isEqualTo("5");
            assertThat(getValue(roomTypeId, date2)).isEqualTo("3");
        }

        @Test
        @DisplayName("복구 후 재차감이 가능하다")
        void shouldAllowDecrementAfterIncrement() {
            // given
            adapter.initializeStock(roomTypeId, Map.of(date1, 1));
            adapter.decrementStock(roomTypeId, List.of(date1)); // 0

            // when
            adapter.incrementStock(roomTypeId, List.of(date1)); // 1
            adapter.decrementStock(roomTypeId, List.of(date1)); // 0

            // then
            assertThat(getValue(roomTypeId, date1)).isEqualTo("0");
        }
    }

    private String getValue(RoomTypeId roomTypeId, LocalDate date) {
        String key = InventoryKeyResolver.resolve(roomTypeId, date);
        Object value = redissonClient.getBucket(key, StringCodec.INSTANCE).get();
        return value != null ? value.toString() : null;
    }
}
