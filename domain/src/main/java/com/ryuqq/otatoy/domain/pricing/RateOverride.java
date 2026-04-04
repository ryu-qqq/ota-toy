package com.ryuqq.otatoy.domain.pricing;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

public class RateOverride {

    private final RateOverrideId id;
    private final RateRuleId rateRuleId;
    private final LocalDate overrideDate;
    private BigDecimal price;
    private String reason;
    private final Instant createdAt;

    private RateOverride(RateOverrideId id, RateRuleId rateRuleId, LocalDate overrideDate,
                         BigDecimal price, String reason, Instant createdAt) {
        this.id = id;
        this.rateRuleId = rateRuleId;
        this.overrideDate = overrideDate;
        this.price = price;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public static RateOverride forNew(RateRuleId rateRuleId, LocalDate overrideDate,
                                       BigDecimal price, String reason, Instant now) {
        if (overrideDate == null) {
            throw new IllegalArgumentException("오버라이드 날짜는 필수입니다");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다");
        }
        return new RateOverride(RateOverrideId.of(null), rateRuleId, overrideDate, price, reason, now);
    }

    public static RateOverride reconstitute(RateOverrideId id, RateRuleId rateRuleId, LocalDate overrideDate,
                                             BigDecimal price, String reason, Instant createdAt) {
        return new RateOverride(id, rateRuleId, overrideDate, price, reason, createdAt);
    }

    public RateOverrideId id() { return id; }
    public RateRuleId rateRuleId() { return rateRuleId; }
    public LocalDate overrideDate() { return overrideDate; }
    public BigDecimal price() { return price; }
    public String reason() { return reason; }
    public Instant createdAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RateOverride r)) return false;
        return id != null && id.equals(r.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
