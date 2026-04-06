package com.ryuqq.otatoy.application.reservation.dto.command;

import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.reservation.GuestInfo;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

import java.time.LocalDate;
import java.util.List;

/**
 * 예약 생성 Command DTO.
 * 필드에 Domain VO를 사용한다 (APP-DTO-001).
 * 인스턴스 메서드 금지 — 순수 데이터 컨테이너 (APP-DTO-001).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record CreateReservationCommand(
    PropertyId propertyId,
    RoomTypeId roomTypeId,
    long customerId,
    GuestInfo guestInfo,
    LocalDate checkIn,
    LocalDate checkOut,
    int guestCount,
    Money totalAmount,
    String bookingSnapshot,
    List<CreateReservationLineCommand> lines
) {

    /**
     * 체크인부터 체크아웃 전날까지의 숙박 날짜 목록을 반환한다.
     * Service에서 Redis 재고 차감/복구에 사용하는 편의 메서드.
     */
    public List<LocalDate> stayDates() {
        return checkIn.datesUntil(checkOut).toList();
    }
}
