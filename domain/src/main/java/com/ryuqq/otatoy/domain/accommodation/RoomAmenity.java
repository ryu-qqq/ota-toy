package com.ryuqq.otatoy.domain.accommodation;

import java.math.BigDecimal;

public record RoomAmenity(
        Long id,
        RoomTypeId roomTypeId,
        String amenityType,
        String name,
        BigDecimal additionalPrice,
        int sortOrder
) {

    public static RoomAmenity of(RoomTypeId roomTypeId, String amenityType, String name,
                                  BigDecimal additionalPrice, int sortOrder) {
        return new RoomAmenity(null, roomTypeId, amenityType, name, additionalPrice, sortOrder);
    }

    public static RoomAmenity reconstitute(Long id, RoomTypeId roomTypeId, String amenityType,
                                            String name, BigDecimal additionalPrice, int sortOrder) {
        return new RoomAmenity(id, roomTypeId, amenityType, name, additionalPrice, sortOrder);
    }

    public boolean isFree() {
        return additionalPrice == null || additionalPrice.compareTo(BigDecimal.ZERO) == 0;
    }
}
