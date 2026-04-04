package com.ryuqq.otatoy.domain.accommodation;

import com.ryuqq.otatoy.domain.common.vo.Money;

import java.util.Objects;

public class PropertyAmenity {

    private final PropertyAmenityId id;
    private final PropertyId propertyId;
    private final AmenityType amenityType;
    private final AmenityName name;
    private final Money additionalPrice;
    private final int sortOrder;

    private PropertyAmenity(PropertyAmenityId id, PropertyId propertyId, AmenityType amenityType,
                            AmenityName name, Money additionalPrice, int sortOrder) {
        this.id = id;
        this.propertyId = propertyId;
        this.amenityType = amenityType;
        this.name = name;
        this.additionalPrice = additionalPrice;
        this.sortOrder = sortOrder;
    }

    public static PropertyAmenity forNew(PropertyId propertyId, AmenityType amenityType, AmenityName name,
                                          Money additionalPrice, int sortOrder) {
        if (amenityType == null) {
            throw new IllegalArgumentException("편의시설 유형은 필수입니다");
        }
        return new PropertyAmenity(PropertyAmenityId.of(null), propertyId, amenityType, name, additionalPrice, sortOrder);
    }

    public static PropertyAmenity reconstitute(PropertyAmenityId id, PropertyId propertyId, AmenityType amenityType,
                                                AmenityName name, Money additionalPrice, int sortOrder) {
        return new PropertyAmenity(id, propertyId, amenityType, name, additionalPrice, sortOrder);
    }

    public boolean isFree() {
        return additionalPrice == null || additionalPrice.isZero();
    }

    public PropertyAmenityId id() { return id; }
    public PropertyId propertyId() { return propertyId; }
    public AmenityType amenityType() { return amenityType; }
    public AmenityName name() { return name; }
    public Money additionalPrice() { return additionalPrice; }
    public int sortOrder() { return sortOrder; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PropertyAmenity p)) return false;
        return id != null && id.equals(p.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
