package com.ryuqq.otatoy.domain.inventory;

import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * 날짜별 객실 재고를 나타내는 Aggregate Root.
 * 특정 객실 유형의 특정 날짜에 대한 전체 수량, 가용 수량, 판매 중지 여부를 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public class Inventory {

    private final InventoryId id;
    private final RoomTypeId roomTypeId;
    private final LocalDate inventoryDate;
    private int totalInventory;
    private int availableCount;
    private boolean stopSell;
    // 낙관적 락 버전. JPA @Version으로 매핑되어 동시 수정 시 OptimisticLockException 발생.
    // 도메인 레이어에서는 버전 비교 로직 없음 — Persistence 레이어에서 자동 처리.
    private int version;
    private final Instant createdAt;
    private Instant updatedAt;

    private Inventory(InventoryId id, RoomTypeId roomTypeId, LocalDate inventoryDate,
                      int totalInventory, int availableCount, boolean stopSell, int version,
                      Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.roomTypeId = roomTypeId;
        this.inventoryDate = inventoryDate;
        this.totalInventory = totalInventory;
        this.availableCount = availableCount;
        this.stopSell = stopSell;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 재고 생성. 가용 수량은 전체 수량과 동일하게 시작한다.
     * @param totalInventory 전체 객실 수 (1 이상)
     */
    public static Inventory forNew(RoomTypeId roomTypeId, LocalDate inventoryDate,
                                    int totalInventory, Instant now) {
        validateRequired(roomTypeId, inventoryDate);
        validateTotalInventory(totalInventory);
        return new Inventory(null, roomTypeId, inventoryDate, totalInventory, totalInventory, false, 0, now, now);
    }

    public static Inventory reconstitute(InventoryId id, RoomTypeId roomTypeId, LocalDate inventoryDate,
                                          int totalInventory, int availableCount, boolean stopSell, int version,
                                          Instant createdAt, Instant updatedAt) {
        return new Inventory(id, roomTypeId, inventoryDate, totalInventory, availableCount, stopSell, version, createdAt, updatedAt);
    }

    /**
     * 재고 1개 차감. 판매 중지 상태이거나 재고가 소진되면 예외 발생.
     */
    public void decrease() {
        decrease(1);
    }

    /**
     * 재고 N개 차감. 다객실 예약 시 roomCount만큼 차감.
     * @param count 차감 수량 (1 이상)
     */
    public void decrease(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("차감 수량은 1 이상이어야 합니다");
        }
        if (stopSell) {
            throw new InventoryStopSellException();
        }
        if (availableCount < count) {
            throw new InventoryExhaustedException();
        }
        this.availableCount -= count;
    }

    /**
     * 재고 1개 복구. 예약 취소 시 사용.
     * 판매 중지 상태와 무관하게 복구 허용 (재고 정합성 우선).
     * 전체 수량(totalInventory)을 초과할 수 없다.
     */
    public void restore() {
        restore(1);
    }

    /**
     * 재고 N개 복구. 다객실 예약 취소 시 사용.
     * 전체 수량(totalInventory)을 초과할 수 없다.
     * @param count 복구 수량 (1 이상)
     */
    public void restore(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("복구 수량은 1 이상이어야 합니다");
        }
        if (availableCount + count > totalInventory) {
            throw new InventoryOverflowException();
        }
        this.availableCount += count;
    }

    /**
     * 파트너가 가용 재고 수량을 직접 설정. 전체 수량을 초과할 수 없다.
     * @param newCount 새로운 가용 재고 수 (0 이상, totalInventory 이하)
     */
    public void updateAvailableCount(int newCount) {
        validateAvailableCount(newCount);
        if (newCount > totalInventory) {
            throw new IllegalArgumentException("가용 재고는 전체 수량(" + totalInventory + ")을 초과할 수 없습니다");
        }
        this.availableCount = newCount;
    }

    /**
     * 전체 객실 수량 변경. 객실 추가/제거 시 사용.
     * 현재 예약된 수량(totalReserved)보다 작게 설정할 수 없다.
     * @param newTotal 새로운 전체 수량 (1 이상)
     */
    public void updateTotalInventory(int newTotal) {
        validateTotalInventory(newTotal);
        int reserved = totalReserved();
        if (newTotal < reserved) {
            throw new IllegalArgumentException(
                    "전체 수량(" + newTotal + ")은 예약된 수량(" + reserved + ") 이상이어야 합니다");
        }
        int diff = newTotal - this.totalInventory;
        this.totalInventory = newTotal;
        this.availableCount += diff;
    }

    /**
     * 현재 예약된 객실 수. 전체 수량 - 가용 수량.
     */
    public int totalReserved() {
        return totalInventory - availableCount;
    }

    private static void validateRequired(RoomTypeId roomTypeId, LocalDate inventoryDate) {
        if (roomTypeId == null) {
            throw new IllegalArgumentException("객실 유형 ID는 필수입니다");
        }
        if (inventoryDate == null) {
            throw new IllegalArgumentException("재고 날짜는 필수입니다");
        }
    }

    private static void validateTotalInventory(int totalInventory) {
        if (totalInventory < 1) {
            throw new IllegalArgumentException("전체 수량은 1 이상이어야 합니다");
        }
    }

    private static void validateAvailableCount(int availableCount) {
        if (availableCount < 0) {
            throw new IllegalArgumentException("가용 재고는 0 이상이어야 합니다");
        }
    }

    public void stopSell() {
        this.stopSell = true;
    }

    public void resumeSell() {
        this.stopSell = false;
    }

    public boolean isAvailable() {
        return !stopSell && availableCount > 0;
    }

    public InventoryId id() { return id; }
    public RoomTypeId roomTypeId() { return roomTypeId; }
    public LocalDate inventoryDate() { return inventoryDate; }
    public int totalInventory() { return totalInventory; }
    public int availableCount() { return availableCount; }
    public boolean isStopSell() { return stopSell; }
    public int version() { return version; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Inventory i)) return false;
        return id != null && id.equals(i.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
