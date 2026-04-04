package com.ryuqq.otatoy.domain.reservation;

import com.ryuqq.otatoy.domain.pricing.RatePlanId;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

public class Reservation {

    private final ReservationId id;
    private final RatePlanId ratePlanId;
    private final String reservationNo;
    private final String guestName;
    private final String guestPhone;
    private final String guestEmail;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private final int guestCount;
    private final BigDecimal totalAmount;
    private ReservationStatus status;
    private String cancelReason;
    private final String bookingSnapshot;
    private final Instant createdAt;
    private Instant cancelledAt;

    private Reservation(ReservationId id, RatePlanId ratePlanId, String reservationNo,
                        String guestName, String guestPhone, String guestEmail,
                        LocalDate checkInDate, LocalDate checkOutDate, int guestCount,
                        BigDecimal totalAmount, ReservationStatus status, String cancelReason,
                        String bookingSnapshot, Instant createdAt, Instant cancelledAt) {
        this.id = id;
        this.ratePlanId = ratePlanId;
        this.reservationNo = reservationNo;
        this.guestName = guestName;
        this.guestPhone = guestPhone;
        this.guestEmail = guestEmail;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestCount = guestCount;
        this.totalAmount = totalAmount;
        this.status = status;
        this.cancelReason = cancelReason;
        this.bookingSnapshot = bookingSnapshot;
        this.createdAt = createdAt;
        this.cancelledAt = cancelledAt;
    }

    public static Reservation forNew(RatePlanId ratePlanId, String reservationNo,
                                      String guestName, String guestPhone, String guestEmail,
                                      LocalDate checkInDate, LocalDate checkOutDate, int guestCount,
                                      BigDecimal totalAmount, String bookingSnapshot, Instant now) {
        if (ratePlanId == null) {
            throw new IllegalArgumentException("요금 정책 ID는 필수입니다");
        }
        if (reservationNo == null || reservationNo.isBlank()) {
            throw new IllegalArgumentException("예약 번호는 필수입니다");
        }
        if (guestName == null || guestName.isBlank()) {
            throw new IllegalArgumentException("투숙객 이름은 필수입니다");
        }
        if (checkInDate == null || checkOutDate == null) {
            throw new IllegalArgumentException("체크인/체크아웃 날짜는 필수입니다");
        }
        if (!checkOutDate.isAfter(checkInDate)) {
            throw new IllegalArgumentException("체크아웃은 체크인 이후여야 합니다");
        }
        if (guestCount <= 0) {
            throw new IllegalArgumentException("투숙 인원은 1명 이상이어야 합니다");
        }
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("총 금액은 0 이상이어야 합니다");
        }
        return new Reservation(null, ratePlanId, reservationNo, guestName, guestPhone, guestEmail,
                checkInDate, checkOutDate, guestCount, totalAmount, ReservationStatus.PENDING,
                null, bookingSnapshot, now, null);
    }

    public static Reservation reconstitute(ReservationId id, RatePlanId ratePlanId, String reservationNo,
                                            String guestName, String guestPhone, String guestEmail,
                                            LocalDate checkInDate, LocalDate checkOutDate, int guestCount,
                                            BigDecimal totalAmount, ReservationStatus status, String cancelReason,
                                            String bookingSnapshot, Instant createdAt, Instant cancelledAt) {
        return new Reservation(id, ratePlanId, reservationNo, guestName, guestPhone, guestEmail,
                checkInDate, checkOutDate, guestCount, totalAmount, status, cancelReason,
                bookingSnapshot, createdAt, cancelledAt);
    }

    public void confirm() {
        if (status != ReservationStatus.PENDING) {
            throw new InvalidReservationStateException();
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    public void cancel(String reason, Instant now) {
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

    public ReservationId id() { return id; }
    public RatePlanId ratePlanId() { return ratePlanId; }
    public String reservationNo() { return reservationNo; }
    public String guestName() { return guestName; }
    public String guestPhone() { return guestPhone; }
    public String guestEmail() { return guestEmail; }
    public LocalDate checkInDate() { return checkInDate; }
    public LocalDate checkOutDate() { return checkOutDate; }
    public int guestCount() { return guestCount; }
    public BigDecimal totalAmount() { return totalAmount; }
    public ReservationStatus status() { return status; }
    public String cancelReason() { return cancelReason; }
    public String bookingSnapshot() { return bookingSnapshot; }
    public Instant createdAt() { return createdAt; }
    public Instant cancelledAt() { return cancelledAt; }

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
