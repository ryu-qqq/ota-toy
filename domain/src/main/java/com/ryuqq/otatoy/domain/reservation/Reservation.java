package com.ryuqq.otatoy.domain.reservation;

import com.ryuqq.otatoy.domain.common.vo.DateRange;
import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class Reservation {

    private static final int MAX_STAY_NIGHTS = 30;

    private final ReservationId id;
    private final RatePlanId ratePlanId;
    private final String reservationNo;
    private final GuestInfo guestInfo;
    private final DateRange stayPeriod;
    private final int guestCount;
    private final Money totalAmount;
    private ReservationStatus status;
    private String cancelReason;
    private final String bookingSnapshot;
    private final Instant createdAt;
    private Instant cancelledAt;
    private final List<ReservationItem> items;

    private Reservation(ReservationId id, RatePlanId ratePlanId, String reservationNo,
                        GuestInfo guestInfo, DateRange stayPeriod, int guestCount,
                        Money totalAmount, ReservationStatus status, String cancelReason,
                        String bookingSnapshot, Instant createdAt, Instant cancelledAt,
                        List<ReservationItem> items) {
        this.id = id;
        this.ratePlanId = ratePlanId;
        this.reservationNo = reservationNo;
        this.guestInfo = guestInfo;
        this.stayPeriod = stayPeriod;
        this.guestCount = guestCount;
        this.totalAmount = totalAmount;
        this.status = status;
        this.cancelReason = cancelReason;
        this.bookingSnapshot = bookingSnapshot;
        this.createdAt = createdAt;
        this.cancelledAt = cancelledAt;
        this.items = items;
    }

    public static Reservation forNew(RatePlanId ratePlanId, String reservationNo,
                                      GuestInfo guestInfo, DateRange stayPeriod,
                                      int guestCount, Money totalAmount,
                                      String bookingSnapshot, List<ReservationItem> items,
                                      LocalDate today, Instant now) {
        if (ratePlanId == null) {
            throw new IllegalArgumentException("요금 정책 ID는 필수입니다");
        }
        if (reservationNo == null || reservationNo.isBlank()) {
            throw new IllegalArgumentException("예약 번호는 필수입니다");
        }
        if (guestInfo == null) {
            throw new IllegalArgumentException("투숙객 정보는 필수입니다");
        }
        if (stayPeriod == null) {
            throw new IllegalArgumentException("숙박 기간은 필수입니다");
        }
        if (guestCount <= 0) {
            throw new IllegalArgumentException("투숙 인원은 1명 이상이어야 합니다");
        }
        if (totalAmount == null) {
            throw new IllegalArgumentException("총 금액은 필수입니다");
        }
        if (stayPeriod.startDate().isBefore(today)) {
            throw new IllegalArgumentException("과거 날짜는 예약할 수 없습니다");
        }
        if (stayPeriod.nights() > MAX_STAY_NIGHTS) {
            throw new IllegalArgumentException("최대 " + MAX_STAY_NIGHTS + "박까지 예약 가능합니다");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("예약 항목은 최소 1개 이상이어야 합니다");
        }
        return new Reservation(null, ratePlanId, reservationNo, guestInfo, stayPeriod,
                guestCount, totalAmount, ReservationStatus.PENDING,
                null, bookingSnapshot, now, null, List.copyOf(items));
    }

    public static Reservation reconstitute(ReservationId id, RatePlanId ratePlanId, String reservationNo,
                                            GuestInfo guestInfo, DateRange stayPeriod, int guestCount,
                                            Money totalAmount, ReservationStatus status, String cancelReason,
                                            String bookingSnapshot, Instant createdAt, Instant cancelledAt,
                                            List<ReservationItem> items) {
        return new Reservation(id, ratePlanId, reservationNo, guestInfo, stayPeriod,
                guestCount, totalAmount, status, cancelReason,
                bookingSnapshot, createdAt, cancelledAt, List.copyOf(items));
    }

    public void confirm() {
        if (status != ReservationStatus.PENDING) {
            throw new InvalidReservationStateException();
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    public void cancel(String reason, Instant now) {
        if (status == ReservationStatus.CANCELLED) {
            throw new ReservationAlreadyCancelledException();
        }
        if (status == ReservationStatus.COMPLETED) {
            throw new ReservationAlreadyCompletedException();
        }
        if (status != ReservationStatus.PENDING && status != ReservationStatus.CONFIRMED) {
            throw new InvalidReservationStateException();
        }
        this.status = ReservationStatus.CANCELLED;
        this.cancelReason = reason;
        this.cancelledAt = now;
    }

    public void complete() {
        if (status != ReservationStatus.CONFIRMED) {
            throw new InvalidReservationStateException();
        }
        this.status = ReservationStatus.COMPLETED;
    }

    public void noShow() {
        if (status != ReservationStatus.CONFIRMED) {
            throw new InvalidReservationStateException();
        }
        this.status = ReservationStatus.NO_SHOW;
    }

    public ReservationId id() { return id; }
    public RatePlanId ratePlanId() { return ratePlanId; }
    public String reservationNo() { return reservationNo; }
    public GuestInfo guestInfo() { return guestInfo; }
    public DateRange stayPeriod() { return stayPeriod; }
    public int guestCount() { return guestCount; }
    public Money totalAmount() { return totalAmount; }
    public ReservationStatus status() { return status; }
    public String cancelReason() { return cancelReason; }
    public String bookingSnapshot() { return bookingSnapshot; }
    public Instant createdAt() { return createdAt; }
    public Instant cancelledAt() { return cancelledAt; }
    public List<ReservationItem> items() { return items; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation r)) return false;
        return id != null && id.equals(r.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
