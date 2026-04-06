package com.ryuqq.otatoy.domain.reservation;

import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

import java.time.Instant;
import java.time.LocalDate;

/**
 * ReservationSession 테스트용 Fixture.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public final class ReservationSessionFixture {

    private ReservationSessionFixture() {}

    public static final PropertyId DEFAULT_PROPERTY_ID = PropertyId.of(1L);
    public static final RoomTypeId DEFAULT_ROOM_TYPE_ID = RoomTypeId.of(1L);
    public static final RatePlanId DEFAULT_RATE_PLAN_ID = RatePlanId.of(1L);
    public static final LocalDate DEFAULT_CHECK_IN = LocalDate.of(2026, 4, 10);
    public static final LocalDate DEFAULT_CHECK_OUT = LocalDate.of(2026, 4, 12);
    public static final int DEFAULT_GUEST_COUNT = 2;
    public static final Money DEFAULT_TOTAL_AMOUNT = Money.of(200_000);
    public static final Instant DEFAULT_NOW = Instant.parse("2026-04-06T00:00:00Z");
    public static final String DEFAULT_IDEMPOTENCY_KEY = "test-idempotency-key-001";

    /**
     * 신규 PENDING 상태 세션
     */
    public static ReservationSession pendingSession() {
        return ReservationSession.forNew(
            DEFAULT_IDEMPOTENCY_KEY, DEFAULT_PROPERTY_ID, DEFAULT_ROOM_TYPE_ID,
            DEFAULT_RATE_PLAN_ID, DEFAULT_CHECK_IN, DEFAULT_CHECK_OUT,
            DEFAULT_GUEST_COUNT, DEFAULT_TOTAL_AMOUNT, DEFAULT_NOW
        );
    }

    /**
     * DB에서 복원된 PENDING 상태 세션
     */
    public static ReservationSession reconstitutedPendingSession(long id) {
        return ReservationSession.reconstitute(
            ReservationSessionId.of(id), DEFAULT_IDEMPOTENCY_KEY,
            DEFAULT_PROPERTY_ID, DEFAULT_ROOM_TYPE_ID,
            DEFAULT_RATE_PLAN_ID, DEFAULT_CHECK_IN, DEFAULT_CHECK_OUT,
            DEFAULT_GUEST_COUNT, DEFAULT_TOTAL_AMOUNT,
            ReservationSessionStatus.PENDING, DEFAULT_NOW, DEFAULT_NOW, null
        );
    }

    /**
     * DB에서 복원된 CONFIRMED 상태 세션
     */
    public static ReservationSession confirmedSession(long id, long reservationId) {
        return ReservationSession.reconstitute(
            ReservationSessionId.of(id), DEFAULT_IDEMPOTENCY_KEY,
            DEFAULT_PROPERTY_ID, DEFAULT_ROOM_TYPE_ID,
            DEFAULT_RATE_PLAN_ID, DEFAULT_CHECK_IN, DEFAULT_CHECK_OUT,
            DEFAULT_GUEST_COUNT, DEFAULT_TOTAL_AMOUNT,
            ReservationSessionStatus.CONFIRMED, DEFAULT_NOW, DEFAULT_NOW, reservationId
        );
    }

    /**
     * 만료된 세션 (생성 시각이 11분 전)
     */
    public static ReservationSession expiredPendingSession(long id, Instant now) {
        Instant createdAt = now.minusSeconds(11 * 60); // 11분 전
        return ReservationSession.reconstitute(
            ReservationSessionId.of(id), DEFAULT_IDEMPOTENCY_KEY,
            DEFAULT_PROPERTY_ID, DEFAULT_ROOM_TYPE_ID,
            DEFAULT_RATE_PLAN_ID, DEFAULT_CHECK_IN, DEFAULT_CHECK_OUT,
            DEFAULT_GUEST_COUNT, DEFAULT_TOTAL_AMOUNT,
            ReservationSessionStatus.PENDING, createdAt, createdAt, null
        );
    }
}
