package com.ryuqq.otatoy.domain.pricing;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

public class Rate {

    private final RateId id;
    private final RatePlanId ratePlanId;
    private final LocalDate rateDate;
    private BigDecimal basePrice;
    private final Instant createdAt;
    private Instant updatedAt;

    private Rate(RateId id, RatePlanId ratePlanId, LocalDate rateDate,
                 BigDecimal basePrice, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.ratePlanId = ratePlanId;
        this.rateDate = rateDate;
        this.basePrice = basePrice;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Rate forNew(RatePlanId ratePlanId, LocalDate rateDate,
                               BigDecimal basePrice, Instant now) {
        if (rateDate == null) {
            throw new IllegalArgumentException("요금 날짜는 필수입니다");
        }
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("기본 가격은 0 이상이어야 합니다");
        }
        return new Rate(RateId.of(null), ratePlanId, rateDate, basePrice, now, now);
    }

    public static Rate reconstitute(RateId id, RatePlanId ratePlanId, LocalDate rateDate,
                                     BigDecimal basePrice, Instant createdAt, Instant updatedAt) {
        return new Rate(id, ratePlanId, rateDate, basePrice, createdAt, updatedAt);
    }

    public void updatePrice(BigDecimal newPrice, Instant now) {
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다");
        }
        this.basePrice = newPrice;
        this.updatedAt = now;
    }

    public RateId id() { return id; }
    public RatePlanId ratePlanId() { return ratePlanId; }
    public LocalDate rateDate() { return rateDate; }
    public BigDecimal basePrice() { return basePrice; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rate r)) return false;
        return id != null && id.equals(r.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
