package com.ryuqq.otatoy.domain.reservation;

import com.ryuqq.otatoy.domain.common.vo.DateRange;
import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.inventory.InventoryId;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Reservation BC 테스트용 Fixture.
 * 다양한 상태의 Reservation, ReservationItem, GuestInfo 등을 생성한다.
 */
public final class ReservationFixture {

    private ReservationFixture() {}

    // === 기본 상수 ===
    public static final LocalDate DEFAULT_TODAY = LocalDate.of(2026, 4, 4);
    public static final Instant DEFAULT_NOW = Instant.parse("2026-04-04T00:00:00Z");
    public static final RatePlanId DEFAULT_RATE_PLAN_ID = RatePlanId.of(1L);
    public static final ReservationNo DEFAULT_RESERVATION_NO = ReservationNo.of("RSV-20260404-001");
    public static final GuestInfo DEFAULT_GUEST_INFO = GuestInfo.of("홍길동", "010-1234-5678", "hong@test.com");
    public static final DateRange DEFAULT_STAY_PERIOD = new DateRange(LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 12));
    public static final int DEFAULT_GUEST_COUNT = 2;
    public static final Money DEFAULT_TOTAL_AMOUNT = Money.of(200_000);
    public static final String DEFAULT_BOOKING_SNAPSHOT = "{\"roomType\":\"deluxe\"}";

    // === ReservationItem Fixture ===

    /**
     * 기본 예약 항목 리스트 (2박: 4/10, 4/11)
     */
    public static List<ReservationItem> defaultItems() {
        return List.of(
                ReservationItem.forNew(null, InventoryId.of(100L), LocalDate.of(2026, 4, 10)),
                ReservationItem.forNew(null, InventoryId.of(101L), LocalDate.of(2026, 4, 11))
        );
    }

    /**
     * 단일 예약 항목 (1박)
     */
    public static ReservationItem singleItem(LocalDate stayDate, long inventoryIdValue) {
        return ReservationItem.forNew(null, InventoryId.of(inventoryIdValue), stayDate);
    }

    /**
     * DB 복원된 예약 항목
     */
    public static ReservationItem reconstitutedItem(long id, long reservationId, long inventoryId, LocalDate stayDate) {
        return ReservationItem.reconstitute(
                ReservationItemId.of(id),
                ReservationId.of(reservationId),
                InventoryId.of(inventoryId),
                stayDate
        );
    }

    // === Reservation Fixture ===

    /**
     * 신규 PENDING 상태 예약
     */
    public static Reservation pendingReservation() {
        return Reservation.forNew(
                DEFAULT_RATE_PLAN_ID, DEFAULT_RESERVATION_NO, DEFAULT_GUEST_INFO, DEFAULT_STAY_PERIOD,
                DEFAULT_GUEST_COUNT, DEFAULT_TOTAL_AMOUNT, DEFAULT_BOOKING_SNAPSHOT, defaultItems(),
                DEFAULT_TODAY, DEFAULT_NOW
        );
    }

    /**
     * 지정 상태의 예약 (reconstitute 사용)
     */
    public static Reservation reservationWithStatus(ReservationStatus status) {
        return Reservation.reconstitute(
                ReservationId.of(1L), DEFAULT_RATE_PLAN_ID, DEFAULT_RESERVATION_NO, DEFAULT_GUEST_INFO,
                DEFAULT_STAY_PERIOD, DEFAULT_GUEST_COUNT, DEFAULT_TOTAL_AMOUNT, status, null,
                DEFAULT_BOOKING_SNAPSHOT, DEFAULT_NOW, null, defaultItems()
        );
    }

    /**
     * CONFIRMED 상태 예약
     */
    public static Reservation confirmedReservation() {
        return reservationWithStatus(ReservationStatus.CONFIRMED);
    }

    /**
     * CANCELLED 상태 예약 (취소 사유 + 취소 시각 포함)
     */
    public static Reservation cancelledReservation() {
        return Reservation.reconstitute(
                ReservationId.of(2L), DEFAULT_RATE_PLAN_ID, DEFAULT_RESERVATION_NO, DEFAULT_GUEST_INFO,
                DEFAULT_STAY_PERIOD, DEFAULT_GUEST_COUNT, DEFAULT_TOTAL_AMOUNT, ReservationStatus.CANCELLED,
                "고객 요청 취소", DEFAULT_BOOKING_SNAPSHOT, DEFAULT_NOW,
                Instant.parse("2026-04-05T10:00:00Z"), defaultItems()
        );
    }

    /**
     * COMPLETED 상태 예약
     */
    public static Reservation completedReservation() {
        return reservationWithStatus(ReservationStatus.COMPLETED);
    }

    /**
     * NO_SHOW 상태 예약
     */
    public static Reservation noShowReservation() {
        return reservationWithStatus(ReservationStatus.NO_SHOW);
    }

    // === GuestInfo Fixture ===

    /**
     * 기본 투숙객 정보
     */
    public static GuestInfo defaultGuestInfo() {
        return DEFAULT_GUEST_INFO;
    }

    /**
     * 최소 정보 투숙객 (이름만)
     */
    public static GuestInfo minimalGuestInfo() {
        return GuestInfo.of("김철수", null, null);
    }
}
