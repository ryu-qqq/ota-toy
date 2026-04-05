package com.ryuqq.otatoy.application.property.dto;

import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertyAmenities;
import com.ryuqq.otatoy.domain.property.PropertyAttributeValues;
import com.ryuqq.otatoy.domain.property.PropertyPhotos;
import com.ryuqq.otatoy.domain.roomtype.RoomTypes;

/**
 * 숙소 상세 조회 도메인 번들.
 * PropertyDetailReadManager가 생성하고, Assembler가 소비한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record PropertyDetailBundle(
    Property property,
    PropertyPhotos photos,
    PropertyAmenities amenities,
    PropertyAttributeValues attributeValues,
    RoomTypes roomTypes
) {}
