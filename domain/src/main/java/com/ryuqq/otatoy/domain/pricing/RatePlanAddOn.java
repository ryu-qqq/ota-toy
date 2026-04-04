package com.ryuqq.otatoy.domain.pricing;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class RatePlanAddOn {

    private final RatePlanAddOnId id;
    private final RatePlanId ratePlanId;
    private AddOnType addOnType;
    private AddOnName name;
    private BigDecimal price;
    private boolean included;
    private final Instant createdAt;

    private RatePlanAddOn(RatePlanAddOnId id, RatePlanId ratePlanId, AddOnType addOnType,
                          AddOnName name, BigDecimal price, boolean included, Instant createdAt) {
        this.id = id;
        this.ratePlanId = ratePlanId;
        this.addOnType = addOnType;
        this.name = name;
        this.price = price;
        this.included = included;
        this.createdAt = createdAt;
    }

    public static RatePlanAddOn forNew(RatePlanId ratePlanId, AddOnType addOnType,
                                        AddOnName name, BigDecimal price, boolean included, Instant now) {
        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Add-on 가격은 0 이상이어야 합니다");
        }
        return new RatePlanAddOn(RatePlanAddOnId.of(null), ratePlanId, addOnType, name, price, included, now);
    }

    public static RatePlanAddOn reconstitute(RatePlanAddOnId id, RatePlanId ratePlanId, AddOnType addOnType,
                                              AddOnName name, BigDecimal price, boolean included, Instant createdAt) {
        return new RatePlanAddOn(id, ratePlanId, addOnType, name, price, included, createdAt);
    }

    public boolean isFree() {
        return price == null || price.compareTo(BigDecimal.ZERO) == 0;
    }

    public RatePlanAddOnId id() { return id; }
    public RatePlanId ratePlanId() { return ratePlanId; }
    public AddOnType addOnType() { return addOnType; }
    public AddOnName name() { return name; }
    public BigDecimal price() { return price; }
    public boolean included() { return included; }
    public Instant createdAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RatePlanAddOn r)) return false;
        return id != null && id.equals(r.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
