package com.ryuqq.otatoy.domain.pricing;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class RatePlanAddOn {

    private final Long id;
    private final RatePlanId ratePlanId;
    private String addOnType;
    private String name;
    private BigDecimal price;
    private boolean included;
    private final Instant createdAt;

    private RatePlanAddOn(Long id, RatePlanId ratePlanId, String addOnType,
                          String name, BigDecimal price, boolean included, Instant createdAt) {
        this.id = id;
        this.ratePlanId = ratePlanId;
        this.addOnType = addOnType;
        this.name = name;
        this.price = price;
        this.included = included;
        this.createdAt = createdAt;
    }

    public static RatePlanAddOn forNew(RatePlanId ratePlanId, String addOnType,
                                        String name, BigDecimal price, boolean included, Instant now) {
        if (addOnType == null || addOnType.isBlank()) {
            throw new IllegalArgumentException("Add-on 유형은 필수입니다");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Add-on 이름은 필수입니다");
        }
        return new RatePlanAddOn(null, ratePlanId, addOnType, name, price, included, now);
    }

    public static RatePlanAddOn reconstitute(Long id, RatePlanId ratePlanId, String addOnType,
                                              String name, BigDecimal price, boolean included, Instant createdAt) {
        return new RatePlanAddOn(id, ratePlanId, addOnType, name, price, included, createdAt);
    }

    public boolean isFree() {
        return price == null || price.compareTo(BigDecimal.ZERO) == 0;
    }

    public Long id() { return id; }
    public RatePlanId ratePlanId() { return ratePlanId; }
    public String addOnType() { return addOnType; }
    public String name() { return name; }
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
