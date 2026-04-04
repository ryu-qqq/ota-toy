package com.ryuqq.otatoy.domain.inventory;

import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Inventory BC 테스트용 Fixture.
 * 다양한 상태의 Inventory 객체를 생성한다.
 */
public final class InventoryFixture {

    private InventoryFixture() {}

    // === 기본 상수 ===
    public static final RoomTypeId DEFAULT_ROOM_TYPE_ID = RoomTypeId.of(1L);
    public static final LocalDate DEFAULT_INVENTORY_DATE = LocalDate.of(2026, 4, 10);
    public static final int DEFAULT_AVAILABLE_COUNT = 10;
    public static final Instant DEFAULT_NOW = Instant.parse("2026-04-04T00:00:00Z");

    // === 신규 생성 Fixture ===

    /**
     * 기본 신규 재고 (가용 10개, 판매 가능)
     */
    public static Inventory defaultInventory() {
        return Inventory.forNew(DEFAULT_ROOM_TYPE_ID, DEFAULT_INVENTORY_DATE, DEFAULT_AVAILABLE_COUNT, DEFAULT_NOW);
    }

    /**
     * 지정 수량의 신규 재고
     */
    public static Inventory inventoryWithCount(int availableCount) {
        return Inventory.forNew(DEFAULT_ROOM_TYPE_ID, DEFAULT_INVENTORY_DATE, availableCount, DEFAULT_NOW);
    }

    /**
     * 지정 날짜의 신규 재고
     */
    public static Inventory inventoryForDate(LocalDate date) {
        return Inventory.forNew(DEFAULT_ROOM_TYPE_ID, date, DEFAULT_AVAILABLE_COUNT, DEFAULT_NOW);
    }

    /**
     * 재고 0개 (소진 상태)
     */
    public static Inventory exhaustedInventory() {
        return Inventory.forNew(DEFAULT_ROOM_TYPE_ID, DEFAULT_INVENTORY_DATE, 0, DEFAULT_NOW);
    }

    // === DB 복원 Fixture ===

    /**
     * DB에서 복원된 기본 재고 (id=1, version=1)
     */
    public static Inventory reconstitutedInventory() {
        return Inventory.reconstitute(
                InventoryId.of(1L), DEFAULT_ROOM_TYPE_ID, DEFAULT_INVENTORY_DATE,
                DEFAULT_AVAILABLE_COUNT, false, 1, DEFAULT_NOW, DEFAULT_NOW
        );
    }

    /**
     * DB에서 복원된 판매 중지 재고
     */
    public static Inventory stopSellInventory() {
        return Inventory.reconstitute(
                InventoryId.of(2L), DEFAULT_ROOM_TYPE_ID, DEFAULT_INVENTORY_DATE,
                DEFAULT_AVAILABLE_COUNT, true, 1, DEFAULT_NOW, DEFAULT_NOW
        );
    }

    /**
     * DB에서 복원된 재고 소진 + 판매 중지 상태
     */
    public static Inventory exhaustedStopSellInventory() {
        return Inventory.reconstitute(
                InventoryId.of(3L), DEFAULT_ROOM_TYPE_ID, DEFAULT_INVENTORY_DATE,
                0, true, 1, DEFAULT_NOW, DEFAULT_NOW
        );
    }

    /**
     * 지정 파라미터로 DB 복원 재고 생성
     */
    public static Inventory reconstituted(long id, long roomTypeId, LocalDate date,
                                           int availableCount, boolean stopSell, int version) {
        return Inventory.reconstitute(
                InventoryId.of(id), RoomTypeId.of(roomTypeId), date,
                availableCount, stopSell, version, DEFAULT_NOW, DEFAULT_NOW
        );
    }
}
