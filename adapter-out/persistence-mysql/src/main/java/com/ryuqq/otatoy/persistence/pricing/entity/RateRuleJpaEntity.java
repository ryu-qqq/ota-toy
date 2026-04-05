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
 * RateRule JPA Entity.
 * 기간별 요금 규칙 데이터를 매핑하는 순수 데이터 매핑 객체.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Entity
@Table(name = "rate_rule")
public class RateRuleJpaEntity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ratePlanId;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal weekdayPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal fridayPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal saturdayPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal sundayPrice;

    protected RateRuleJpaEntity() {
        super();
    }

    private RateRuleJpaEntity(Long id, Long ratePlanId, LocalDate startDate, LocalDate endDate,
                               BigDecimal basePrice, BigDecimal weekdayPrice,
                               BigDecimal fridayPrice, BigDecimal saturdayPrice, BigDecimal sundayPrice,
                               Instant createdAt, Instant updatedAt, Instant deletedAt) {
        super(createdAt, updatedAt, deletedAt);
        this.id = id;
        this.ratePlanId = ratePlanId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.basePrice = basePrice;
        this.weekdayPrice = weekdayPrice;
        this.fridayPrice = fridayPrice;
        this.saturdayPrice = saturdayPrice;
        this.sundayPrice = sundayPrice;
    }

    public static RateRuleJpaEntity create(Long id, Long ratePlanId, LocalDate startDate, LocalDate endDate,
                                            BigDecimal basePrice, BigDecimal weekdayPrice,
                                            BigDecimal fridayPrice, BigDecimal saturdayPrice, BigDecimal sundayPrice,
                                            Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new RateRuleJpaEntity(id, ratePlanId, startDate, endDate,
                basePrice, weekdayPrice, fridayPrice, saturdayPrice, sundayPrice,
                createdAt, updatedAt, deletedAt);
    }

    public Long getId() { return id; }
    public Long getRatePlanId() { return ratePlanId; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public BigDecimal getBasePrice() { return basePrice; }
    public BigDecimal getWeekdayPrice() { return weekdayPrice; }
    public BigDecimal getFridayPrice() { return fridayPrice; }
    public BigDecimal getSaturdayPrice() { return saturdayPrice; }
    public BigDecimal getSundayPrice() { return sundayPrice; }
}
