package com.ryuqq.otatoy.domain.inventory;

import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryTest {

    private static final RoomTypeId ROOM_TYPE_ID = RoomTypeId.of(1L);
    private static final LocalDate INVENTORY_DATE = LocalDate.of(2026, 4, 10);
    private static final Instant NOW = Instant.parse("2026-04-04T00:00:00Z");

    // ========================================
    // T-1: 생성 검증 -- forNew()
    // ========================================

    @Nested
    @DisplayName("T-1: 생성 검증 -- forNew()")
    class Creation {

        @Test
        @DisplayName("정상 생성 시 id는 null이고 stopSell은 false이다")
        void shouldCreateWithNullIdAndStopSellFalse() {
            Inventory inventory = Inventory.forNew(ROOM_TYPE_ID, INVENTORY_DATE, 10, NOW);

            assertThat(inventory.id()).isNull();
            assertThat(inventory.roomTypeId()).isEqualTo(ROOM_TYPE_ID);
            assertThat(inventory.inventoryDate()).isEqualTo(INVENTORY_DATE);
            assertThat(inventory.availableCount()).isEqualTo(10);
            assertThat(inventory.isStopSell()).isFalse();
            assertThat(inventory.version()).isZero();
        }

        @Test
        @DisplayName("가용 재고 0으로 생성 가능하다")
        void shouldAllowZeroAvailableCount() {
            Inventory inventory = Inventory.forNew(ROOM_TYPE_ID, INVENTORY_DATE, 0, NOW);

            assertThat(inventory.availableCount()).isZero();
            assertThat(inventory.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("roomTypeId가 null이면 예외 발생")
        void shouldThrowWhenRoomTypeIdIsNull() {
            assertThatThrownBy(() -> Inventory.forNew(null, INVENTORY_DATE, 10, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("객실 유형 ID");
        }

        @Test
        @DisplayName("inventoryDate가 null이면 예외 발생")
        void shouldThrowWhenInventoryDateIsNull() {
            assertThatThrownBy(() -> Inventory.forNew(ROOM_TYPE_ID, null, 10, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("재고 날짜");
        }

        @Test
        @DisplayName("availableCount가 음수이면 예외 발생")
        void shouldThrowWhenAvailableCountIsNegative() {
            assertThatThrownBy(() -> Inventory.forNew(ROOM_TYPE_ID, INVENTORY_DATE, -1, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("0 이상");
        }
    }

    // ========================================
    // T-2: reconstitute 검증
    // ========================================

    @Nested
    @DisplayName("T-2: DB 복원 -- reconstitute()")
    class Reconstitution {

        @Test
        @DisplayName("모든 필드가 그대로 복원된다")
        void shouldReconstitueAllFields() {
            InventoryId inventoryId = InventoryId.of(1L);
            Inventory inventory = Inventory.reconstitute(inventoryId, ROOM_TYPE_ID, INVENTORY_DATE, 5, true, 3, NOW, NOW);

            assertThat(inventory.id()).isEqualTo(inventoryId);
            assertThat(inventory.roomTypeId()).isEqualTo(ROOM_TYPE_ID);
            assertThat(inventory.inventoryDate()).isEqualTo(INVENTORY_DATE);
            assertThat(inventory.availableCount()).isEqualTo(5);
            assertThat(inventory.isStopSell()).isTrue();
            assertThat(inventory.version()).isEqualTo(3);
        }

        @Test
        @DisplayName("비즈니스 검증을 수행하지 않는다 (음수 재고도 복원 가능)")
        void shouldNotValidateOnReconstitute() {
            // reconstitute는 DB 데이터를 그대로 복원하므로 검증하지 않아야 한다
            Inventory inventory = Inventory.reconstitute(
                    InventoryId.of(1L), ROOM_TYPE_ID, INVENTORY_DATE, -1, false, 1, NOW, NOW
            );
            assertThat(inventory.availableCount()).isEqualTo(-1);
        }
    }

    // ========================================
    // T-3: decrease() 재고 차감 검증
    // ========================================

    @Nested
    @DisplayName("T-3: 재고 차감 -- decrease()")
    class Decrease {

        @Test
        @DisplayName("1개 차감 시 availableCount가 1 감소한다")
        void shouldDecreaseByOne() {
            Inventory inventory = Inventory.forNew(ROOM_TYPE_ID, INVENTORY_DATE, 10, NOW);
            inventory.decrease();

            assertThat(inventory.availableCount()).isEqualTo(9);
        }

        @Test
        @DisplayName("N개 차감 시 availableCount가 N 감소한다")
        void shouldDecreaseByCount() {
            Inventory inventory = Inventory.forNew(ROOM_TYPE_ID, INVENTORY_DATE, 10, NOW);
            inventory.decrease(3);

            assertThat(inventory.availableCount()).isEqualTo(7);
        }

        @Test
        @DisplayName("전체 재고를 차감하면 0이 된다")
        void shouldDecreaseToZero() {
            Inventory inventory = Inventory.forNew(ROOM_TYPE_ID, INVENTORY_DATE, 5, NOW);
            inventory.decrease(5);

            assertThat(inventory.availableCount()).isZero();
            assertThat(inventory.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("재고가 부족하면 InventoryExhaustedException 발생")
        void shouldThrowWhenInsufficientStock() {
            Inventory inventory = Inventory.forNew(ROOM_TYPE_ID, INVENTORY_DATE, 2, NOW);

            assertThatThrownBy(() -> inventory.decrease(3))
                    .isInstanceOf(InventoryExhaustedException.class);
        }

        @Test
        @DisplayName("재고 0에서 차감하면 InventoryExhaustedException 발생")
        void shouldThrowWhenZeroStock() {
            Inventory inventory = Inventory.forNew(ROOM_TYPE_ID, INVENTORY_DATE, 0, NOW);

            assertThatThrownBy(() -> inventory.decrease())
                    .isInstanceOf(InventoryExhaustedException.class);
        }

        @Test
        @DisplayName("판매 중지 상태에서 차감하면 InventoryStopSellException 발생")
        void shouldThrowWhenStopSell() {
            Inventory inventory = Inventory.reconstitute(
                    InventoryId.of(1L), ROOM_TYPE_ID, INVENTORY_DATE, 10, true, 1, NOW, NOW
            );

            assertThatThrownBy(() -> inventory.decrease())
                    .isInstanceOf(InventoryStopSellException.class);
        }

        @Test
        @DisplayName("차감 수량이 0이면 예외 발생")
        void shouldThrowWhenCountIsZero() {
            Inventory inventory = Inventory.forNew(ROOM_TYPE_ID, INVENTORY_DATE, 10, NOW);

            assertThatThrownBy(() -> inventory.decrease(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("1 이상");
        }

        @Test
        @DisplayName("차감 수량이 음수이면 예외 발생")
        void shouldThrowWhenCountIsNegative() {
            Inventory inventory = Inventory.forNew(ROOM_TYPE_ID, INVENTORY_DATE, 10, NOW);

            assertThatThrownBy(() -> inventory.decrease(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("1 이상");
        }
    }

    // ========================================
    // T-4: restore() 재고 복구 검증
    // ========================================

    @Nested
    @DisplayName("T-4: 재고 복구 -- restore()")
    class Restore {

        @Test
        @DisplayName("1개 복구 시 availableCount가 1 증가한다")
        void shouldRestoreByOne() {
            Inventory inventory = Inventory.forNew(ROOM_TYPE_ID, INVENTORY_DATE, 5, NOW);
            inventory.restore();

            assertThat(inventory.availableCount()).isEqualTo(6);
        }

        @Test
        @DisplayName("N개 복구 시 availableCount가 N 증가한다")
        void shouldRestoreByCount() {
            Inventory inventory = Inventory.forNew(ROOM_TYPE_ID, INVENTORY_DATE, 5, NOW);
            inventory.restore(3);

            assertThat(inventory.availableCount()).isEqualTo(8);
        }

        @Test
        @DisplayName("판매 중지 상태에서도 복구 가능하다 (재고 정합성 우선)")
        void shouldRestoreEvenWhenStopSell() {
            Inventory inventory = Inventory.reconstitute(
                    InventoryId.of(1L), ROOM_TYPE_ID, INVENTORY_DATE, 5, true, 1, NOW, NOW
            );
            inventory.restore(2);

            assertThat(inventory.availableCount()).isEqualTo(7);
            assertThat(inventory.isStopSell()).isTrue();
        }

        @Test
        @DisplayName("재고 0에서 복구하면 다시 가용 상태가 된다")
        void shouldBecomeAvailableAfterRestore() {
            Inventory inventory = Inventory.forNew(ROOM_TYPE_ID, INVENTORY_DATE, 0, NOW);
            assertThat(inventory.isAvailable()).isFalse();

            inventory.restore();
            assertThat(inventory.isAvailable()).isTrue();
            assertThat(inventory.availableCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("복구 수량이 0이면 예외 발생")
        void shouldThrowWhenCountIsZero() {
            Inventory inventory = Inventory.forNew(ROOM_TYPE_ID, INVENTORY_DATE, 5, NOW);

            assertThatThrownBy(() -> inventory.restore(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("1 이상");
        }

        @Test
        @DisplayName("복구 수량이 음수이면 예외 발생")
        void shouldThrowWhenCountIsNegative() {
            Inventory inventory = Inventory.forNew(ROOM_TYPE_ID, INVENTORY_DATE, 5, NOW);

            assertThatThrownBy(() -> inventory.restore(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("1 이상");
        }
    }

    // ========================================
    // T-5: updateAvailableCount() 재고 수량 설정
    // ========================================

    @Nested
    @DisplayName("T-5: 재고 수량 설정 -- updateAvailableCount()")
    class UpdateAvailableCount {

        @Test
        @DisplayName("새로운 수량으로 설정된다")
        void shouldUpdateCount() {
            Inventory inventory = Inventory.forNew(ROOM_TYPE_ID, INVENTORY_DATE, 10, NOW);
            inventory.updateAvailableCount(20);

            assertThat(inventory.availableCount()).isEqualTo(20);
        }

        @Test
        @DisplayName("0으로 설정 가능하다")
        void shouldAllowZero() {
            Inventory inventory = Inventory.forNew(ROOM_TYPE_ID, INVENTORY_DATE, 10, NOW);
            inventory.updateAvailableCount(0);

            assertThat(inventory.availableCount()).isZero();
            assertThat(inventory.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("음수로 설정하면 예외 발생")
        void shouldThrowWhenNegative() {
            Inventory inventory = Inventory.forNew(ROOM_TYPE_ID, INVENTORY_DATE, 10, NOW);

            assertThatThrownBy(() -> inventory.updateAvailableCount(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("0 이상");
        }
    }

    // ========================================
    // T-6: stopSell / resumeSell 판매 제어
    // ========================================

    @Nested
    @DisplayName("T-6: 판매 제어 -- stopSell() / resumeSell()")
    class SellControl {

        @Test
        @DisplayName("stopSell 후 isStopSell은 true이고 isAvailable은 false이다")
        void shouldStopSell() {
            Inventory inventory = Inventory.forNew(ROOM_TYPE_ID, INVENTORY_DATE, 10, NOW);
            inventory.stopSell();

            assertThat(inventory.isStopSell()).isTrue();
            assertThat(inventory.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("resumeSell 후 isStopSell은 false이고 재고가 있으면 isAvailable은 true이다")
        void shouldResumeSell() {
            Inventory inventory = Inventory.reconstitute(
                    InventoryId.of(1L), ROOM_TYPE_ID, INVENTORY_DATE, 10, true, 1, NOW, NOW
            );
            inventory.resumeSell();

            assertThat(inventory.isStopSell()).isFalse();
            assertThat(inventory.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("재고 0에서 resumeSell해도 isAvailable은 false이다")
        void shouldNotBeAvailableWithZeroStockAfterResume() {
            Inventory inventory = Inventory.reconstitute(
                    InventoryId.of(1L), ROOM_TYPE_ID, INVENTORY_DATE, 0, true, 1, NOW, NOW
            );
            inventory.resumeSell();

            assertThat(inventory.isStopSell()).isFalse();
            assertThat(inventory.isAvailable()).isFalse();
        }
    }

    // ========================================
    // T-7: isAvailable() 가용성 판단
    // ========================================

    @Nested
    @DisplayName("T-7: 가용성 판단 -- isAvailable()")
    class Availability {

        @Test
        @DisplayName("재고 있고 판매 가능이면 true")
        void shouldBeAvailable() {
            Inventory inventory = Inventory.forNew(ROOM_TYPE_ID, INVENTORY_DATE, 10, NOW);
            assertThat(inventory.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("재고 0이면 false")
        void shouldNotBeAvailableWhenZeroStock() {
            Inventory inventory = Inventory.forNew(ROOM_TYPE_ID, INVENTORY_DATE, 0, NOW);
            assertThat(inventory.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("판매 중지이면 재고 있어도 false")
        void shouldNotBeAvailableWhenStopSell() {
            Inventory inventory = Inventory.reconstitute(
                    InventoryId.of(1L), ROOM_TYPE_ID, INVENTORY_DATE, 10, true, 1, NOW, NOW
            );
            assertThat(inventory.isAvailable()).isFalse();
        }
    }

    // ========================================
    // T-8: equals/hashCode
    // ========================================

    @Nested
    @DisplayName("T-8: 동등성 -- equals() / hashCode()")
    class Equality {

        @Test
        @DisplayName("같은 id를 가진 두 객체는 동등하다")
        void shouldBeEqualWithSameId() {
            Inventory a = Inventory.reconstitute(InventoryId.of(1L), ROOM_TYPE_ID, INVENTORY_DATE, 10, false, 1, NOW, NOW);
            Inventory b = Inventory.reconstitute(InventoryId.of(1L), RoomTypeId.of(2L), INVENTORY_DATE.plusDays(1), 5, true, 2, NOW, NOW);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("다른 id를 가진 두 객체는 동등하지 않다")
        void shouldNotBeEqualWithDifferentId() {
            Inventory a = Inventory.reconstitute(InventoryId.of(1L), ROOM_TYPE_ID, INVENTORY_DATE, 10, false, 1, NOW, NOW);
            Inventory b = Inventory.reconstitute(InventoryId.of(2L), ROOM_TYPE_ID, INVENTORY_DATE, 10, false, 1, NOW, NOW);

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("id가 null인 두 객체는 동등하지 않다 (forNew 직후)")
        void shouldNotBeEqualWhenBothIdNull() {
            Inventory a = Inventory.forNew(ROOM_TYPE_ID, INVENTORY_DATE, 10, NOW);
            Inventory b = Inventory.forNew(ROOM_TYPE_ID, INVENTORY_DATE, 10, NOW);

            assertThat(a).isNotEqualTo(b);
        }
    }

    // ========================================
    // T-9: 복합 시나리오
    // ========================================

    @Nested
    @DisplayName("T-9: 복합 시나리오")
    class ComplexScenarios {

        @Test
        @DisplayName("차감 -> 복구 -> 가용성 확인 시나리오")
        void decreaseThenRestoreShouldMaintainConsistency() {
            Inventory inventory = Inventory.forNew(ROOM_TYPE_ID, INVENTORY_DATE, 3, NOW);

            inventory.decrease(2);
            assertThat(inventory.availableCount()).isEqualTo(1);
            assertThat(inventory.isAvailable()).isTrue();

            inventory.decrease();
            assertThat(inventory.availableCount()).isZero();
            assertThat(inventory.isAvailable()).isFalse();

            inventory.restore(2);
            assertThat(inventory.availableCount()).isEqualTo(2);
            assertThat(inventory.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("판매 중지 -> 복구 -> 재개 시나리오")
        void stopSellThenRestoreThenResumeShouldWork() {
            Inventory inventory = Inventory.forNew(ROOM_TYPE_ID, INVENTORY_DATE, 5, NOW);

            // 판매 중지
            inventory.stopSell();
            assertThat(inventory.isAvailable()).isFalse();

            // 판매 중지 중에도 복구 가능 (예약 취소로 인한 복구)
            inventory.restore(2);
            assertThat(inventory.availableCount()).isEqualTo(7);
            assertThat(inventory.isAvailable()).isFalse(); // 아직 stopSell

            // 판매 재개
            inventory.resumeSell();
            assertThat(inventory.isAvailable()).isTrue();
            assertThat(inventory.availableCount()).isEqualTo(7);
        }

        @Test
        @DisplayName("파트너 수량 업데이트 후 차감 시나리오")
        void updateCountThenDecreaseShouldWork() {
            Inventory inventory = Inventory.forNew(ROOM_TYPE_ID, INVENTORY_DATE, 0, NOW);
            assertThat(inventory.isAvailable()).isFalse();

            inventory.updateAvailableCount(5);
            assertThat(inventory.isAvailable()).isTrue();

            inventory.decrease(3);
            assertThat(inventory.availableCount()).isEqualTo(2);
        }
    }
}
