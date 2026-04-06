package com.ryuqq.otatoy.persistence.reservation.entity;

import com.ryuqq.otatoy.persistence.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Reservation JPA Entity.
 * 예약 기본 정보를 매핑하는 순수 데이터 매핑 객체.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Entity
@Table(name = "reservation")
public class ReservationJpaEntity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private long customerId;

    @Column(nullable = false, length = 50, unique = true)
    private String reservationNo;

    @Column(nullable = false, length = 100)
    private String guestName;

    @Column(nullable = false, length = 30)
    private String guestPhone;

    @Column(length = 200)
    private String guestEmail;

    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column(nullable = false)
    private LocalDate checkOutDate;

    @Column(nullable = false)
    private int guestCount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(length = 500)
    private String cancelReason;

    @Column(columnDefinition = "JSON")
    private String bookingSnapshot;

    private Instant cancelledAt;

    protected ReservationJpaEntity() {
        super();
    }

    private ReservationJpaEntity(Long id, long customerId, String reservationNo,
                                  String guestName, String guestPhone, String guestEmail,
                                  LocalDate checkInDate, LocalDate checkOutDate,
                                  int guestCount, BigDecimal totalAmount, String status,
                                  String cancelReason, String bookingSnapshot,
                                  Instant cancelledAt, Instant createdAt, Instant updatedAt,
                                  Instant deletedAt) {
        super(createdAt, updatedAt, deletedAt);
        this.id = id;
        this.customerId = customerId;
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
        this.cancelledAt = cancelledAt;
    }

    public static ReservationJpaEntity create(Long id, long customerId, String reservationNo,
                                               String guestName, String guestPhone, String guestEmail,
                                               LocalDate checkInDate, LocalDate checkOutDate,
                                               int guestCount, BigDecimal totalAmount, String status,
                                               String cancelReason, String bookingSnapshot,
                                               Instant cancelledAt, Instant createdAt, Instant updatedAt,
                                               Instant deletedAt) {
        return new ReservationJpaEntity(id, customerId, reservationNo, guestName, guestPhone, guestEmail,
                checkInDate, checkOutDate, guestCount, totalAmount, status,
                cancelReason, bookingSnapshot, cancelledAt, createdAt, updatedAt, deletedAt);
    }

    public Long getId() { return id; }
    public long getCustomerId() { return customerId; }
    public String getReservationNo() { return reservationNo; }
    public String getGuestName() { return guestName; }
    public String getGuestPhone() { return guestPhone; }
    public String getGuestEmail() { return guestEmail; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public int getGuestCount() { return guestCount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public String getCancelReason() { return cancelReason; }
    public String getBookingSnapshot() { return bookingSnapshot; }
    public Instant getCancelledAt() { return cancelledAt; }
}
