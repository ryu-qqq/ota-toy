package com.ryuqq.otatoy.domain.reservation;

import com.ryuqq.otatoy.domain.common.vo.DateRange;
import com.ryuqq.otatoy.domain.common.vo.Money;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * 예약을 나타내는 Aggregate Root.
 * 고객 식별자, 투숙객 정보, 숙박 기간, 총 금액, 예약 상태를 관리한다.
 * 하나의 예약에 여러 ReservationLine을 포함하여 서로 다른 객실 유형을 조합 예약할 수 있다.
 * 최대 연박 제한은 30박이다.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 * @see ReservationLine 객실 유형별 예약 단위
 * @see ReservationItem 날짜별 재고·요금 연결
 */
public class Reservation {

    private static final int MAX_STAY_NIGHTS = 30;

    private final ReservationId id;
    private final long customerId;
    private final ReservationNo reservationNo;
    private final GuestInfo guestInfo;
    private final DateRange stayPeriod;
    private final int guestCount;
    private final Money totalAmount;
    private ReservationStatus status;
    private String cancelReason;
    private final String bookingSnapshot;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant cancelledAt;
    private final List<ReservationLine> lines;

    private Reservation(ReservationId id, long customerId, ReservationNo reservationNo,
                        GuestInfo guestInfo, DateRange stayPeriod, int guestCount,
                        Money totalAmount, ReservationStatus status, String cancelReason,
                        String bookingSnapshot, Instant createdAt, Instant updatedAt,
                        Instant cancelledAt, List<ReservationLine> lines) {
        this.id = id;
        this.customerId = customerId;
        this.reservationNo = reservationNo;
        this.guestInfo = guestInfo;
        this.stayPeriod = stayPeriod;
        this.guestCount = guestCount;
        this.totalAmount = totalAmount;
        this.status = status;
        this.cancelReason = cancelReason;
        this.bookingSnapshot = bookingSnapshot;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.cancelledAt = cancelledAt;
        this.lines = lines;
    }

    public static Reservation forNew(long customerId, ReservationNo reservationNo,
                                      GuestInfo guestInfo, DateRange stayPeriod,
                                      int guestCount, Money totalAmount,
                                      String bookingSnapshot, List<ReservationLine> lines,
                                      LocalDate today, Instant now) {
        validateRequired(guestInfo, stayPeriod, totalAmount, lines);
        validateCustomerId(customerId);
        validateGuestCount(guestCount);
        validateStayPeriod(stayPeriod, today);
        return new Reservation(null, customerId, reservationNo, guestInfo, stayPeriod,
                guestCount, totalAmount, ReservationStatus.PENDING,
                null, bookingSnapshot, now, now, null, List.copyOf(lines));
    }

    private static void validateRequired(GuestInfo guestInfo, DateRange stayPeriod,
                                          Money totalAmount, List<ReservationLine> lines) {
        if (guestInfo == null) {
            throw new IllegalArgumentException("투숙객 정보는 필수입니다");
        }
        if (stayPeriod == null) {
            throw new IllegalArgumentException("숙박 기간은 필수입니다");
        }
        if (totalAmount == null) {
            throw new IllegalArgumentException("총 금액은 필수입니다");
        }
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("예약 라인은 최소 1개 이상이어야 합니다");
        }
    }

    private static void validateCustomerId(long customerId) {
        if (customerId <= 0) {
            throw new IllegalArgumentException("고객 ID는 1 이상이어야 합니다");
        }
    }

    private static void validateGuestCount(int guestCount) {
        if (guestCount <= 0) {
            throw new IllegalArgumentException("투숙 인원은 1명 이상이어야 합니다");
        }
    }

    private static void validateStayPeriod(DateRange stayPeriod, LocalDate today) {
        if (stayPeriod.startDate().isBefore(today)) {
            throw new IllegalArgumentException("과거 날짜는 예약할 수 없습니다");
        }
        if (stayPeriod.nights() > MAX_STAY_NIGHTS) {
            throw new IllegalArgumentException("최대 " + MAX_STAY_NIGHTS + "박까지 예약 가능합니다");
        }
    }

    public static Reservation reconstitute(ReservationId id, long customerId, ReservationNo reservationNo,
                                            GuestInfo guestInfo, DateRange stayPeriod, int guestCount,
                                            Money totalAmount, ReservationStatus status, String cancelReason,
                                            String bookingSnapshot, Instant createdAt, Instant updatedAt,
                                            Instant cancelledAt, List<ReservationLine> lines) {
        return new Reservation(id, customerId, reservationNo, guestInfo, stayPeriod,
                guestCount, totalAmount, status, cancelReason,
                bookingSnapshot, createdAt, updatedAt, cancelledAt, List.copyOf(lines));
    }

    public void confirm(Instant now) {
        this.status = status.transitTo(ReservationStatus.CONFIRMED);
        this.updatedAt = now;
    }

    public void cancel(String reason, Instant now) {
        if (status == ReservationStatus.CANCELLED) {
            throw new ReservationAlreadyCancelledException();
        }
        if (status == ReservationStatus.COMPLETED) {
            throw new ReservationAlreadyCompletedException();
        }
        this.status = status.transitTo(ReservationStatus.CANCELLED);
        this.cancelReason = reason;
        this.cancelledAt = now;
        this.updatedAt = now;
    }

    public void complete(Instant now) {
        this.status = status.transitTo(ReservationStatus.COMPLETED);
        this.updatedAt = now;
    }

    public void noShow(Instant now) {
        this.status = status.transitTo(ReservationStatus.NO_SHOW);
        this.updatedAt = now;
    }

    public ReservationId id() { return id; }
    public long customerId() { return customerId; }
    public ReservationNo reservationNo() { return reservationNo; }
    public GuestInfo guestInfo() { return guestInfo; }
    public DateRange stayPeriod() { return stayPeriod; }
    public int guestCount() { return guestCount; }
    public Money totalAmount() { return totalAmount; }
    public ReservationStatus status() { return status; }
    public String cancelReason() { return cancelReason; }
    public String bookingSnapshot() { return bookingSnapshot; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public Instant cancelledAt() { return cancelledAt; }
    public List<ReservationLine> lines() { return lines; }

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
