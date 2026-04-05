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
 * RateOverride JPA Entity.
 * 특정 날짜 요금 덮어쓰기 데이터를 매핑하는 순수 데이터 매핑 객체.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Entity
@Table(name = "rate_override")
public class RateOverrideJpaEntity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long rateRuleId;

    @Column(nullable = false)
    private LocalDate overrideDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(length = 500)
    private String reason;

    protected RateOverrideJpaEntity() {
        super();
    }

    private RateOverrideJpaEntity(Long id, Long rateRuleId, LocalDate overrideDate,
                                   BigDecimal price, String reason,
                                   Instant createdAt, Instant updatedAt, Instant deletedAt) {
        super(createdAt, updatedAt, deletedAt);
        this.id = id;
        this.rateRuleId = rateRuleId;
        this.overrideDate = overrideDate;
        this.price = price;
        this.reason = reason;
    }

    public static RateOverrideJpaEntity create(Long id, Long rateRuleId, LocalDate overrideDate,
                                                BigDecimal price, String reason,
                                                Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new RateOverrideJpaEntity(id, rateRuleId, overrideDate, price, reason,
                createdAt, updatedAt, deletedAt);
    }

    public Long getId() { return id; }
    public Long getRateRuleId() { return rateRuleId; }
    public LocalDate getOverrideDate() { return overrideDate; }
    public BigDecimal getPrice() { return price; }
    public String getReason() { return reason; }
}
