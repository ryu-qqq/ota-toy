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

    public void decrease() {
        if (stopSell) {
            throw new InventoryStopSellException();
        }
        if (availableCount <= 0) {
            throw new InventoryExhaustedException();
        }
        this.availableCount--;
    }

    public void restore() {
        this.availableCount++;
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
