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

/**
 * ReservationLine JPA Entity.
 * 예약 라인 (객실 유형별 예약 단위)을 매핑하는 순수 데이터 매핑 객체.
 * Long FK 전략: reservationId, ratePlanId.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Entity
@Table(name = "reservation_line")
public class ReservationLineJpaEntity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reservationId;

    @Column(nullable = false)
    private Long ratePlanId;

    @Column(nullable = false)
    private int roomCount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotalAmount;

    protected ReservationLineJpaEntity() {
        super();
    }

    private ReservationLineJpaEntity(Long id, Long reservationId, Long ratePlanId,
                                      int roomCount, BigDecimal subtotalAmount,
                                      Instant createdAt, Instant updatedAt, Instant deletedAt) {
        super(createdAt, updatedAt, deletedAt);
        this.id = id;
        this.reservationId = reservationId;
        this.ratePlanId = ratePlanId;
        this.roomCount = roomCount;
        this.subtotalAmount = subtotalAmount;
    }

    public static ReservationLineJpaEntity create(Long id, Long reservationId, Long ratePlanId,
                                                    int roomCount, BigDecimal subtotalAmount,
                                                    Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new ReservationLineJpaEntity(id, reservationId, ratePlanId,
                roomCount, subtotalAmount, createdAt, updatedAt, deletedAt);
    }

    public Long getId() { return id; }
    public Long getReservationId() { return reservationId; }
    public Long getRatePlanId() { return ratePlanId; }
    public int getRoomCount() { return roomCount; }
    public BigDecimal getSubtotalAmount() { return subtotalAmount; }
}
