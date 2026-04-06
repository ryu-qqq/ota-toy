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
 * ReservationItem JPA Entity.
 * 날짜별 재고/요금 연결을 매핑하는 순수 데이터 매핑 객체.
 * Long FK 전략: reservationId, reservationLineId, inventoryId.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Entity
@Table(name = "reservation_item")
public class ReservationItemJpaEntity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reservationId;

    @Column(nullable = false)
    private Long reservationLineId;

    @Column(nullable = false)
    private Long inventoryId;

    @Column(nullable = false)
    private LocalDate stayDate;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal nightlyRate;

    protected ReservationItemJpaEntity() {
        super();
    }

    private ReservationItemJpaEntity(Long id, Long reservationId, Long reservationLineId,
                                      Long inventoryId, LocalDate stayDate, BigDecimal nightlyRate,
                                      Instant createdAt, Instant updatedAt, Instant deletedAt) {
        super(createdAt, updatedAt, deletedAt);
        this.id = id;
        this.reservationId = reservationId;
        this.reservationLineId = reservationLineId;
        this.inventoryId = inventoryId;
        this.stayDate = stayDate;
        this.nightlyRate = nightlyRate;
    }

    public static ReservationItemJpaEntity create(Long id, Long reservationId, Long reservationLineId,
                                                    Long inventoryId, LocalDate stayDate, BigDecimal nightlyRate,
                                                    Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new ReservationItemJpaEntity(id, reservationId, reservationLineId,
                inventoryId, stayDate, nightlyRate, createdAt, updatedAt, deletedAt);
    }

    public Long getId() { return id; }
    public Long getReservationId() { return reservationId; }
    public Long getReservationLineId() { return reservationLineId; }
    public Long getInventoryId() { return inventoryId; }
    public LocalDate getStayDate() { return stayDate; }
    public BigDecimal getNightlyRate() { return nightlyRate; }
}
