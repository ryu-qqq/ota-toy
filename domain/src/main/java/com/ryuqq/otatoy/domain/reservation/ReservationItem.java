package com.ryuqq.otatoy.domain.reservation;

import com.ryuqq.otatoy.domain.inventory.InventoryId;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 예약의 날짜별 항목을 나타내는 엔티티.
 * 하나의 예약에서 각 숙박 날짜와 해당 재고를 연결한다.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public class ReservationItem {

    private final ReservationItemId id;
    private final ReservationId reservationId;
    private final InventoryId inventoryId;
    private final LocalDate stayDate;

    private ReservationItem(ReservationItemId id, ReservationId reservationId, InventoryId inventoryId, LocalDate stayDate) {
        this.id = id;
        this.reservationId = reservationId;
        this.inventoryId = inventoryId;
        this.stayDate = stayDate;
    }

    public static ReservationItem forNew(ReservationId reservationId, InventoryId inventoryId, LocalDate stayDate) {
        validate(inventoryId, stayDate);
        return new ReservationItem(ReservationItemId.of(null), reservationId, inventoryId, stayDate);
    }

    private static void validate(InventoryId inventoryId, LocalDate stayDate) {
        if (stayDate == null) {
            throw new IllegalArgumentException("숙박 날짜는 필수입니다");
        }
        if (inventoryId == null) {
            throw new IllegalArgumentException("재고 ID는 필수입니다");
        }
    }

    public static ReservationItem reconstitute(ReservationItemId id, ReservationId reservationId, InventoryId inventoryId, LocalDate stayDate) {
        return new ReservationItem(id, reservationId, inventoryId, stayDate);
    }

    public ReservationItemId id() { return id; }
    public ReservationId reservationId() { return reservationId; }
    public InventoryId inventoryId() { return inventoryId; }
    public LocalDate stayDate() { return stayDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReservationItem r)) return false;
        return id != null && id.equals(r.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
