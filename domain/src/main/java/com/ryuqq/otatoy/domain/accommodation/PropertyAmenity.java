package com.ryuqq.otatoy.domain.accommodation;

import java.math.BigDecimal;

public record PropertyAmenity(
        Long id,
        PropertyId propertyId,
        String amenityType,
        String name,
        BigDecimal additionalPrice,
        int sortOrder
) {

    public static PropertyAmenity of(PropertyId propertyId, String amenityType, String name,
                                      BigDecimal additionalPrice, int sortOrder) {
        return new PropertyAmenity(null, propertyId, amenityType, name, additionalPrice, sortOrder);
    }

    public static PropertyAmenity reconstitute(Long id, PropertyId propertyId, String amenityType,
                                                String name, BigDecimal additionalPrice, int sortOrder) {
        return new PropertyAmenity(id, propertyId, amenityType, name, additionalPrice, sortOrder);
    }

    public boolean isFree() {
        return additionalPrice == null || additionalPrice.compareTo(BigDecimal.ZERO) == 0;
    }
}
