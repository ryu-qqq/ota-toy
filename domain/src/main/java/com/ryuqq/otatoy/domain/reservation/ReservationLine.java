package com.ryuqq.otatoy.domain.reservation;

import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * 예약 라인을 나타내는 엔티티.
 * 하나의 예약에서 특정 객실 유형(요금제)에 대한 예약 단위를 표현한다.
 * roomCount를 통해 같은 유형의 객실을 여러 개 예약할 수 있다.
 * 하나의 예약에 서로 다른 객실 유형(디럭스 2개 + 스탠다드 1개)을 조합할 수 있다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 * @see ReservationItem 날짜별 재고·요금 연결
 */
public class ReservationLine {

    private final ReservationLineId id;
    private final ReservationId reservationId;
    private final RatePlanId ratePlanId;
    private final int roomCount;
    private final Money subtotalAmount;
    private final Instant createdAt;
    private Instant updatedAt;
    private final List<ReservationItem> items;

    private ReservationLine(ReservationLineId id, ReservationId reservationId,
                            RatePlanId ratePlanId, int roomCount, Money subtotalAmount,
                            Instant createdAt, Instant updatedAt, List<ReservationItem> items) {
        this.id = id;
        this.reservationId = reservationId;
        this.ratePlanId = ratePlanId;
        this.roomCount = roomCount;
        this.subtotalAmount = subtotalAmount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.items = items;
    }

    public static ReservationLine forNew(ReservationId reservationId, RatePlanId ratePlanId,
                                          int roomCount, Money subtotalAmount,
                                          List<ReservationItem> items, Instant now) {
        validateRequired(ratePlanId, subtotalAmount, items);
        validateRoomCount(roomCount);
        return new ReservationLine(ReservationLineId.forNew(), reservationId, ratePlanId,
                roomCount, subtotalAmount, now, now, List.copyOf(items));
    }

    public static ReservationLine reconstitute(ReservationLineId id, ReservationId reservationId,
                                                RatePlanId ratePlanId, int roomCount,
                                                Money subtotalAmount, Instant createdAt,
                                                Instant updatedAt, List<ReservationItem> items) {
        return new ReservationLine(id, reservationId, ratePlanId, roomCount,
                subtotalAmount, createdAt, updatedAt, List.copyOf(items));
    }

    private static void validateRequired(RatePlanId ratePlanId, Money subtotalAmount,
                                          List<ReservationItem> items) {
        if (ratePlanId == null) {
            throw new IllegalArgumentException("요금 정책 ID는 필수입니다");
        }
        if (subtotalAmount == null) {
            throw new IllegalArgumentException("소계 금액은 필수입니다");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("예약 항목은 최소 1개 이상이어야 합니다");
        }
    }

    private static void validateRoomCount(int roomCount) {
        if (roomCount <= 0) {
            throw new IllegalArgumentException("객실 수는 1개 이상이어야 합니다");
        }
    }

    public ReservationLineId id() { return id; }
    public ReservationId reservationId() { return reservationId; }
    public RatePlanId ratePlanId() { return ratePlanId; }
    public int roomCount() { return roomCount; }
    public Money subtotalAmount() { return subtotalAmount; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public List<ReservationItem> items() { return items; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReservationLine r)) return false;
        return id != null && id.equals(r.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
