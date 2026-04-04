package com.ryuqq.otatoy.domain.pricing;

import java.math.BigDecimal;
import java.util.Objects;

public class RatePlanAddOn {

    private final Long id;
    private final RatePlanId ratePlanId;
    private String addOnType;
    private String name;
    private BigDecimal price;
    private boolean included;

    private RatePlanAddOn(Long id, RatePlanId ratePlanId, String addOnType,
                          String name, BigDecimal price, boolean included) {
        this.id = id;
        this.ratePlanId = ratePlanId;
        this.addOnType = addOnType;
        this.name = name;
        this.price = price;
        this.included = included;
    }

    public static RatePlanAddOn forNew(RatePlanId ratePlanId, String addOnType,
                                        String name, BigDecimal price, boolean included) {
        if (addOnType == null || addOnType.isBlank()) {
            throw new IllegalArgumentException("Add-on 유형은 필수입니다");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Add-on 이름은 필수입니다");
        }
        return new RatePlanAddOn(null, ratePlanId, addOnType, name, price, included);
    }

    public static RatePlanAddOn reconstitute(Long id, RatePlanId ratePlanId, String addOnType,
                                              String name, BigDecimal price, boolean included) {
        return new RatePlanAddOn(id, ratePlanId, addOnType, name, price, included);
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
