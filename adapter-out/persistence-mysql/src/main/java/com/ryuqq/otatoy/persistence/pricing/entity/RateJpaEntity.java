package com.ryuqq.otatoy.persistence.pricing.entity;

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
 * Rate JPA Entity.
 * 날짜별 확정 요금 데이터를 매핑하는 순수 데이터 매핑 객체.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Entity
@Table(name = "rate")
public class RateJpaEntity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ratePlanId;

    @Column(nullable = false)
    private LocalDate rateDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    protected RateJpaEntity() {
        super();
    }

    private RateJpaEntity(Long id, Long ratePlanId, LocalDate rateDate, BigDecimal basePrice,
                           Instant createdAt, Instant updatedAt, Instant deletedAt) {
        super(createdAt, updatedAt, deletedAt);
        this.id = id;
        this.ratePlanId = ratePlanId;
        this.rateDate = rateDate;
        this.basePrice = basePrice;
    }

    public static RateJpaEntity create(Long id, Long ratePlanId, LocalDate rateDate, BigDecimal basePrice,
                                        Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new RateJpaEntity(id, ratePlanId, rateDate, basePrice, createdAt, updatedAt, deletedAt);
    }

    public Long getId() { return id; }
    public Long getRatePlanId() { return ratePlanId; }
    public LocalDate getRateDate() { return rateDate; }
    public BigDecimal getBasePrice() { return basePrice; }
}
