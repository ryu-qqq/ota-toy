package com.ryuqq.otatoy.application.inventory.manager;

import com.ryuqq.otatoy.application.inventory.port.out.redis.InventoryRedisPort;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

/**
 * isRedisConnectionFailure()가 클래스명으로 판별하므로,
 * 테스트용 예외 클래스를 정의하여 시뮬레이션한다.
 */
class RedisConnectionException extends RuntimeException {
    RedisConnectionException(String message) { super(message); }
}

class ConnectionTimeoutException extends RuntimeException {
    ConnectionTimeoutException(String message) { super(message); }
}

/**
 * InventoryClientManager 단위 테스트.
 * InventoryRedisPort와 InventoryCommandManager를 Mock으로 대체하여
 * Redis 재고 차감/복구 및 DB 폴백 로직을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class InventoryClientManagerTest {

    @Mock
    InventoryRedisPort inventoryRedisPort;

    @Mock
    InventoryCommandManager inventoryCommandManager;

    @InjectMocks
    InventoryClientManager clientManager;

    private static final RoomTypeId ROOM_TYPE_ID = RoomTypeId.of(1L);
    private static final List<LocalDate> DATES = List.of(
        LocalDate.of(2026, 4, 10),
        LocalDate.of(2026, 4, 11)
    );

    @Nested
    @DisplayName("decrementStock")
    class DecrementStock {

        @Test
        @DisplayName("Redis 정상 동작 시 Redis를 통해 재고를 차감한다")
        void shouldDecrementViaRedisWhenAvailable() {
            // given
            willDoNothing().given(inventoryRedisPort).decrementStock(ROOM_TYPE_ID, DATES);

            // when
            clientManager.decrementStock(ROOM_TYPE_ID, DATES);

            // then
            then(inventoryRedisPort).should().decrementStock(ROOM_TYPE_ID, DATES);
            then(inventoryCommandManager).should(never()).decrementAvailable(ROOM_TYPE_ID, DATES);
        }

        @Test
        @DisplayName("Redis 연결 장애 시 DB 폴백으로 재고를 차감한다")
        void shouldFallbackToDbWhenRedisConnectionFails() {
            // given -- 클래스명에 "Redis"가 포함된 예외로 시뮬레이션
            willThrow(new RedisConnectionException("Redis 연결 실패"))
                .given(inventoryRedisPort).decrementStock(ROOM_TYPE_ID, DATES);

            // when
            clientManager.decrementStock(ROOM_TYPE_ID, DATES);

            // then
            then(inventoryCommandManager).should().decrementAvailable(ROOM_TYPE_ID, DATES);
        }

        @Test
        @DisplayName("Redis Timeout 장애 시 DB 폴백으로 재고를 차감한다")
        void shouldFallbackToDbWhenRedisTimeoutOccurs() {
            // given -- 클래스명에 "Timeout"이 포함된 예외로 시뮬레이션
            willThrow(new ConnectionTimeoutException("연결 타임아웃"))
                .given(inventoryRedisPort).decrementStock(ROOM_TYPE_ID, DATES);

            // when
            clientManager.decrementStock(ROOM_TYPE_ID, DATES);

            // then
            then(inventoryCommandManager).should().decrementAvailable(ROOM_TYPE_ID, DATES);
        }

        @Test
        @DisplayName("Redis 비즈니스 예외(재고 부족 등)는 그대로 전파된다")
        void shouldPropagateNonConnectionException() {
            // given
            willThrow(new IllegalStateException("재고 부족"))
                .given(inventoryRedisPort).decrementStock(ROOM_TYPE_ID, DATES);

            // when & then
            assertThatThrownBy(() -> clientManager.decrementStock(ROOM_TYPE_ID, DATES))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("재고 부족");
            then(inventoryCommandManager).should(never()).decrementAvailable(ROOM_TYPE_ID, DATES);
        }
    }

    @Nested
    @DisplayName("incrementStock")
    class IncrementStock {

        @Test
        @DisplayName("Redis 정상 동작 시 Redis를 통해 재고를 복구한다")
        void shouldIncrementViaRedis() {
            // given
            willDoNothing().given(inventoryRedisPort).incrementStock(ROOM_TYPE_ID, DATES);

            // when
            clientManager.incrementStock(ROOM_TYPE_ID, DATES);

            // then
            then(inventoryRedisPort).should().incrementStock(ROOM_TYPE_ID, DATES);
        }

        @Test
        @DisplayName("Redis 장애 시 예외를 삼키고 조용히 실패한다")
        void shouldSwallowExceptionWhenRedisFails() {
            // given
            willThrow(new RedisConnectionException("Redis 복구 실패"))
                .given(inventoryRedisPort).incrementStock(ROOM_TYPE_ID, DATES);

            // when & then
            assertThatCode(() -> clientManager.incrementStock(ROOM_TYPE_ID, DATES))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("initializeStock")
    class InitializeStock {

        @Test
        @DisplayName("Redis 정상 동작 시 Redis를 통해 재고를 초기화한다")
        void shouldInitializeViaRedis() {
            // given
            Map<LocalDate, Integer> dateStockMap = Map.of(
                LocalDate.of(2026, 4, 10), 10,
                LocalDate.of(2026, 4, 11), 8
            );
            willDoNothing().given(inventoryRedisPort).initializeStock(ROOM_TYPE_ID, dateStockMap);

            // when
            clientManager.initializeStock(ROOM_TYPE_ID, dateStockMap);

            // then
            then(inventoryRedisPort).should().initializeStock(ROOM_TYPE_ID, dateStockMap);
        }

        @Test
        @DisplayName("Redis 장애 시 예외를 삼키고 조용히 실패한다")
        void shouldSwallowExceptionWhenRedisFails() {
            // given
            Map<LocalDate, Integer> dateStockMap = Map.of(LocalDate.of(2026, 4, 10), 10);
            willThrow(new RedisConnectionException("Redis 초기화 실패"))
                .given(inventoryRedisPort).initializeStock(ROOM_TYPE_ID, dateStockMap);

            // when & then
            assertThatCode(() -> clientManager.initializeStock(ROOM_TYPE_ID, dateStockMap))
                .doesNotThrowAnyException();
        }
    }
}
