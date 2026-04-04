package com.ryuqq.otatoy.domain.reservation;

import java.time.LocalDate;

public record ReservationItem(
        Long id,
        Long reservationId,
        Long inventoryId,
        LocalDate stayDate
) {

    public ReservationItem {
        if (stayDate == null) {
            throw new IllegalArgumentException("숙박 날짜는 필수입니다");
        }
        if (inventoryId == null) {
            throw new IllegalArgumentException("재고 ID는 필수입니다");
        }
    }

    public static ReservationItem of(Long reservationId, Long inventoryId, LocalDate stayDate) {
        return new ReservationItem(null, reservationId, inventoryId, stayDate);
    }

    public static ReservationItem reconstitute(Long id, Long reservationId, Long inventoryId, LocalDate stayDate) {
        return new ReservationItem(id, reservationId, inventoryId, stayDate);
    }
}
