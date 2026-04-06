package com.ryuqq.otatoy.domain.reservation;

import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * 예약 세션을 나타내는 Aggregate Root.
 * 2단계 예약 프로세스의 1단계에서 생성되며, Redis 재고 선점 상태를 추적한다.
 * TTL은 10분이며, 만료 시 재고가 복구된다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 * @see ReservationSessionStatus 세션 상태
 */
public class ReservationSession {

    private static final long SESSION_TTL_MINUTES = 10;

    private final ReservationSessionId id;
    private final String idempotencyKey;
    private final PropertyId propertyId;
    private final RoomTypeId roomTypeId;
    private final RatePlanId ratePlanId;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final int guestCount;
    private final Money totalAmount;
    private ReservationSessionStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    private Long reservationId;

    private ReservationSession(ReservationSessionId id, String idempotencyKey,
                               PropertyId propertyId, RoomTypeId roomTypeId,
                               RatePlanId ratePlanId, LocalDate checkIn, LocalDate checkOut,
                               int guestCount, Money totalAmount, ReservationSessionStatus status,
                               Instant createdAt, Instant updatedAt, Long reservationId) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.propertyId = propertyId;
        this.roomTypeId = roomTypeId;
        this.ratePlanId = ratePlanId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.guestCount = guestCount;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.reservationId = reservationId;
    }

    /**
     * 신규 예약 세션 생성. 재고 선점 후 호출한다.
     */
    public static ReservationSession forNew(String idempotencyKey, PropertyId propertyId,
                                             RoomTypeId roomTypeId, RatePlanId ratePlanId,
                                             LocalDate checkIn, LocalDate checkOut,
                                             int guestCount, Money totalAmount, Instant now) {
        validateIdempotencyKey(idempotencyKey);
        validateRequired(propertyId, roomTypeId, ratePlanId, checkIn, checkOut, totalAmount);
        validateGuestCount(guestCount);
        validateDates(checkIn, checkOut);
        return new ReservationSession(null, idempotencyKey, propertyId, roomTypeId, ratePlanId,
                checkIn, checkOut, guestCount, totalAmount,
                ReservationSessionStatus.PENDING, now, now, null);
    }

    /**
     * DB 복원용.
     */
    public static ReservationSession reconstitute(ReservationSessionId id, String idempotencyKey,
                                                   PropertyId propertyId, RoomTypeId roomTypeId,
                                                   RatePlanId ratePlanId, LocalDate checkIn,
                                                   LocalDate checkOut, int guestCount,
                                                   Money totalAmount, ReservationSessionStatus status,
                                                   Instant createdAt, Instant updatedAt,
                                                   Long reservationId) {
        return new ReservationSession(id, idempotencyKey, propertyId, roomTypeId, ratePlanId,
                checkIn, checkOut, guestCount, totalAmount,
                status, createdAt, updatedAt, reservationId);
    }

    /**
     * 예약 확정. PENDING 상태이고 미만료인 경우에만 가능하다.
     */
    public void confirm(Long reservationId, Instant now) {
        if (status != ReservationSessionStatus.PENDING) {
            throw new InvalidSessionStateException();
        }
        if (isExpired(now)) {
            throw new ReservationSessionExpiredException();
        }
        this.status = ReservationSessionStatus.CONFIRMED;
        this.reservationId = reservationId;
        this.updatedAt = now;
    }

    /**
     * 세션 만료 처리. PENDING 상태가 아니면 무시한다 (멱등).
     */
    public void expire(Instant now) {
        if (status != ReservationSessionStatus.PENDING) {
            return;
        }
        this.status = ReservationSessionStatus.EXPIRED;
        this.updatedAt = now;
    }

    /**
     * 세션이 만료되었는지 확인한다 (생성 후 10분 경과).
     */
    public boolean isExpired(Instant now) {
        return Duration.between(createdAt, now).toMinutes() >= SESSION_TTL_MINUTES;
    }

    /**
     * 체크인부터 체크아웃 전날까지의 숙박 날짜 목록을 반환한다.
     */
    public List<LocalDate> stayDates() {
        return checkIn.datesUntil(checkOut).toList();
    }

    private static void validateIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("멱등키는 필수입니다");
        }
    }

    private static void validateRequired(PropertyId propertyId, RoomTypeId roomTypeId,
                                          RatePlanId ratePlanId, LocalDate checkIn,
                                          LocalDate checkOut, Money totalAmount) {
        if (propertyId == null) throw new IllegalArgumentException("숙소 ID는 필수입니다");
        if (roomTypeId == null) throw new IllegalArgumentException("객실 유형 ID는 필수입니다");
        if (ratePlanId == null) throw new IllegalArgumentException("요금 정책 ID는 필수입니다");
        if (checkIn == null) throw new IllegalArgumentException("체크인 날짜는 필수입니다");
        if (checkOut == null) throw new IllegalArgumentException("체크아웃 날짜는 필수입니다");
        if (totalAmount == null) throw new IllegalArgumentException("총 금액은 필수입니다");
    }

    private static void validateGuestCount(int guestCount) {
        if (guestCount <= 0) {
            throw new IllegalArgumentException("투숙 인원은 1명 이상이어야 합니다");
        }
    }

    private static void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("체크아웃은 체크인보다 뒤여야 합니다");
        }
    }

    // === Getters ===
    public ReservationSessionId id() { return id; }
    public String idempotencyKey() { return idempotencyKey; }
    public PropertyId propertyId() { return propertyId; }
    public RoomTypeId roomTypeId() { return roomTypeId; }
    public RatePlanId ratePlanId() { return ratePlanId; }
    public LocalDate checkIn() { return checkIn; }
    public LocalDate checkOut() { return checkOut; }
    public int guestCount() { return guestCount; }
    public Money totalAmount() { return totalAmount; }
    public ReservationSessionStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public Long reservationId() { return reservationId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReservationSession s)) return false;
        return id != null && id.equals(s.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
