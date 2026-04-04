package com.ryuqq.otatoy.domain.roomtype;

import com.ryuqq.otatoy.domain.accommodation.AmenityName;
import com.ryuqq.otatoy.domain.accommodation.AmenityType;

import com.ryuqq.otatoy.domain.common.vo.Money;

import java.util.Objects;

public class RoomAmenity {

    private final RoomAmenityId id;
    private final RoomTypeId roomTypeId;
    private final AmenityType amenityType;
    private final AmenityName name;
    private final Money additionalPrice;
    private final int sortOrder;

    private RoomAmenity(RoomAmenityId id, RoomTypeId roomTypeId, AmenityType amenityType,
                        AmenityName name, Money additionalPrice, int sortOrder) {
        this.id = id;
        this.roomTypeId = roomTypeId;
        this.amenityType = amenityType;
        this.name = name;
        this.additionalPrice = additionalPrice;
        this.sortOrder = sortOrder;
    }

    public static RoomAmenity forNew(RoomTypeId roomTypeId, AmenityType amenityType, AmenityName name,
                                      Money additionalPrice, int sortOrder) {
        validate(amenityType);
        return new RoomAmenity(RoomAmenityId.of(null), roomTypeId, amenityType, name, additionalPrice, sortOrder);
    }

    private static void validate(AmenityType amenityType) {
        if (amenityType == null) {
            throw new IllegalArgumentException("편의시설 유형은 필수입니다");
        }
    }

    public static RoomAmenity reconstitute(RoomAmenityId id, RoomTypeId roomTypeId, AmenityType amenityType,
                                            AmenityName name, Money additionalPrice, int sortOrder) {
        return new RoomAmenity(id, roomTypeId, amenityType, name, additionalPrice, sortOrder);
    }

    public boolean isFree() {
        return additionalPrice == null || additionalPrice.isZero();
    }

    public RoomAmenityId id() { return id; }
    public RoomTypeId roomTypeId() { return roomTypeId; }
    public AmenityType amenityType() { return amenityType; }
    public AmenityName name() { return name; }
    public Money additionalPrice() { return additionalPrice; }
    public int sortOrder() { return sortOrder; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoomAmenity r)) return false;
        return id != null && id.equals(r.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
