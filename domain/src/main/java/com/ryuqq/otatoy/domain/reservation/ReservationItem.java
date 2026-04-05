package com.ryuqq.otatoy.domain.reservation;

import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.inventory.InventoryId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * 예약 라인의 날짜별 항목을 나타내는 엔티티.
 * 특정 숙박 날짜에 대한 재고 연결과 해당 박의 1실 가격을 담는다.
 * ReservationLine에 포함되어 관리된다.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public class ReservationItem {

    private final ReservationItemId id;
    private final InventoryId inventoryId;
    private final LocalDate stayDate;
    private final Money nightlyRate;
    private final Instant createdAt;
    private Instant updatedAt;

    private ReservationItem(ReservationItemId id, InventoryId inventoryId,
                             LocalDate stayDate, Money nightlyRate,
                             Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.inventoryId = inventoryId;
        this.stayDate = stayDate;
        this.nightlyRate = nightlyRate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ReservationItem forNew(InventoryId inventoryId, LocalDate stayDate,
                                          Money nightlyRate, Instant now) {
        validate(inventoryId, stayDate, nightlyRate);
        return new ReservationItem(ReservationItemId.forNew(), inventoryId,
                stayDate, nightlyRate, now, now);
    }

    private static void validate(InventoryId inventoryId, LocalDate stayDate, Money nightlyRate) {
        if (stayDate == null) {
            throw new IllegalArgumentException("숙박 날짜는 필수입니다");
        }
        if (inventoryId == null) {
            throw new IllegalArgumentException("재고 ID는 필수입니다");
        }
        if (nightlyRate == null) {
            throw new IllegalArgumentException("1박 요금은 필수입니다");
        }
    }

    public static ReservationItem reconstitute(ReservationItemId id, InventoryId inventoryId,
                                                LocalDate stayDate, Money nightlyRate,
                                                Instant createdAt, Instant updatedAt) {
        return new ReservationItem(id, inventoryId, stayDate, nightlyRate, createdAt, updatedAt);
    }

    public ReservationItemId id() { return id; }
    public InventoryId inventoryId() { return inventoryId; }
    public LocalDate stayDate() { return stayDate; }
    public Money nightlyRate() { return nightlyRate; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

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
