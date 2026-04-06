package com.ryuqq.otatoy.persistence.reservation.entity;

import com.ryuqq.otatoy.persistence.entity.BaseAuditEntity;
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
 * ReservationSession JPA Entity.
 * 2단계 예약 프로세스의 1단계 (재고 선점) 세션을 매핑하는 순수 데이터 매핑 객체.
 * soft delete 없음 — BaseAuditEntity 상속.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Entity
@Table(name = "reservation_session")
public class ReservationSessionJpaEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64, unique = true)
    private String idempotencyKey;

    @Column(nullable = false)
    private Long propertyId;

    @Column(nullable = false)
    private Long roomTypeId;

    @Column(nullable = false)
    private Long ratePlanId;

    @Column(nullable = false)
    private LocalDate checkIn;

    @Column(nullable = false)
    private LocalDate checkOut;

    @Column(nullable = false)
    private int guestCount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 20)
    private String status;

    private Long reservationId;

    protected ReservationSessionJpaEntity() {
        super();
    }

    private ReservationSessionJpaEntity(Long id, String idempotencyKey, Long propertyId,
                                         Long roomTypeId, Long ratePlanId, LocalDate checkIn,
                                         LocalDate checkOut, int guestCount, BigDecimal totalAmount,
                                         String status, Long reservationId,
                                         Instant createdAt, Instant updatedAt) {
        super(createdAt, updatedAt);
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
        this.reservationId = reservationId;
    }

    public static ReservationSessionJpaEntity create(Long id, String idempotencyKey, Long propertyId,
                                                      Long roomTypeId, Long ratePlanId, LocalDate checkIn,
                                                      LocalDate checkOut, int guestCount, BigDecimal totalAmount,
                                                      String status, Long reservationId,
                                                      Instant createdAt, Instant updatedAt) {
        return new ReservationSessionJpaEntity(id, idempotencyKey, propertyId, roomTypeId,
                ratePlanId, checkIn, checkOut, guestCount, totalAmount,
                status, reservationId, createdAt, updatedAt);
    }

    public Long getId() { return id; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public Long getPropertyId() { return propertyId; }
    public Long getRoomTypeId() { return roomTypeId; }
    public Long getRatePlanId() { return ratePlanId; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public int getGuestCount() { return guestCount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public Long getReservationId() { return reservationId; }
}
