package com.ryuqq.otatoy.domain.pricing;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * 요금 정책에 연결된 부가 서비스를 나타내는 엔티티.
 * 조식, 스파 이용권 등 추가 서비스와 그 가격을 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
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
        validatePriceAndInclusion(price, included);
        return new RatePlanAddOn(RatePlanAddOnId.of(null), ratePlanId, addOnType, name, price, included, now);
    }

    private static void validatePriceAndInclusion(BigDecimal price, boolean included) {
        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Add-on 가격은 0 이상이어야 합니다");
        }
        if (included && price != null && price.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalArgumentException("포함된 Add-on은 별도 가격을 가질 수 없습니다");
        }
        if (!included && (price == null || price.compareTo(BigDecimal.ZERO) == 0)) {
            throw new IllegalArgumentException("별도 구매 Add-on은 가격이 필수입니다");
        }
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
