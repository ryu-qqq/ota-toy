package com.ryuqq.otatoy.domain.inventory;

import com.ryuqq.otatoy.domain.accommodation.RoomTypeId;

import java.time.LocalDate;
import java.util.Objects;

public class Inventory {

    private final InventoryId id;
    private final RoomTypeId roomTypeId;
    private final LocalDate inventoryDate;
    private int availableCount;
    private boolean stopSell;
    private int version;

    private Inventory(InventoryId id, RoomTypeId roomTypeId, LocalDate inventoryDate,
                      int availableCount, boolean stopSell, int version) {
        this.id = id;
        this.roomTypeId = roomTypeId;
        this.inventoryDate = inventoryDate;
        this.availableCount = availableCount;
        this.stopSell = stopSell;
        this.version = version;
    }

    public static Inventory forNew(RoomTypeId roomTypeId, LocalDate inventoryDate,
                                    int availableCount) {
        if (roomTypeId == null) {
            throw new IllegalArgumentException("객실 유형 ID는 필수입니다");
        }
        if (inventoryDate == null) {
            throw new IllegalArgumentException("재고 날짜는 필수입니다");
        }
        if (availableCount < 0) {
            throw new IllegalArgumentException("가용 재고는 0 이상이어야 합니다");
        }
        return new Inventory(null, roomTypeId, inventoryDate, availableCount, false, 0);
    }

    public static Inventory reconstitute(InventoryId id, RoomTypeId roomTypeId, LocalDate inventoryDate,
                                          int availableCount, boolean stopSell, int version) {
        return new Inventory(id, roomTypeId, inventoryDate, availableCount, stopSell, version);
    }

    /**
     * 재고 1개 차감. 판매 중지 상태이거나 재고가 소진되면 예외 발생.
     */
    public void decrease() {
        decrease(1);
    }

    /**
     * 재고 N개 차감. 다박 예약 등 복수 차감 시 사용.
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
     */
    public void restore() {
        restore(1);
    }

    /**
     * 재고 N개 복구. 다박 예약 취소 시 사용.
     * @param count 복구 수량 (1 이상)
     */
    public void restore(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("복구 수량은 1 이상이어야 합니다");
        }
        this.availableCount += count;
    }

    /**
     * 파트너가 재고 수량을 직접 설정. 초기 세팅 또는 수량 변경 시 사용.
     * @param newCount 새로운 가용 재고 수 (0 이상)
     */
    public void updateAvailableCount(int newCount) {
        if (newCount < 0) {
            throw new IllegalArgumentException("가용 재고는 0 이상이어야 합니다");
        }
        this.availableCount = newCount;
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
    public int availableCount() { return availableCount; }
    public boolean isStopSell() { return stopSell; }
    public int version() { return version; }

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
