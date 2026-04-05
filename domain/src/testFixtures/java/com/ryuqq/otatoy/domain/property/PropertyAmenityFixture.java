package com.ryuqq.otatoy.domain.property;

import com.ryuqq.otatoy.domain.accommodation.AmenityName;
import com.ryuqq.otatoy.domain.accommodation.AmenityType;
import com.ryuqq.otatoy.domain.common.vo.Money;

import java.time.Instant;

public final class PropertyAmenityFixture {

    private PropertyAmenityFixture() {}

    public static final PropertyId DEFAULT_PROPERTY_ID = PropertyId.of(1L);
    public static final Instant DEFAULT_NOW = Instant.parse("2026-04-05T10:00:00Z");

    public static PropertyAmenity aPropertyAmenity() {
        return PropertyAmenity.forNew(
            DEFAULT_PROPERTY_ID, AmenityType.WIFI, AmenityName.of("와이파이"),
            Money.of(0), 1, DEFAULT_NOW
        );
    }

    public static PropertyAmenity aPaidAmenity(AmenityType type, String name, int price, int sortOrder) {
        return PropertyAmenity.forNew(
            DEFAULT_PROPERTY_ID, type, AmenityName.of(name),
            Money.of(price), sortOrder, DEFAULT_NOW
        );
    }

    public static PropertyAmenity anAmenityForProperty(long propertyId, AmenityType type, String name, int sortOrder) {
        return PropertyAmenity.forNew(
            PropertyId.of(propertyId), type, AmenityName.of(name),
            Money.of(0), sortOrder, DEFAULT_NOW
        );
    }
}
