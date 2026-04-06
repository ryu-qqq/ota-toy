package com.ryuqq.otatoy.application.reservation.dto.command;

import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

import java.time.LocalDate;
import java.util.List;

/**
 * 예약 세션 생성 Command DTO (1단계: 재고 선점).
 * 필드에 Domain VO를 사용한다 (APP-DTO-001).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record CreateReservationSessionCommand(
    String idempotencyKey,
    PropertyId propertyId,
    RoomTypeId roomTypeId,
    RatePlanId ratePlanId,
    LocalDate checkIn,
    LocalDate checkOut,
    int guestCount,
    Money totalAmount
) {

    /**
     * 체크인부터 체크아웃 전날까지의 숙박 날짜 목록을 반환한다.
     */
    public List<LocalDate> stayDates() {
        return checkIn.datesUntil(checkOut).toList();
    }
}
