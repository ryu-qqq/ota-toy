package com.ryuqq.otatoy.application.property.dto.result;

import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertyAmenities;
import com.ryuqq.otatoy.domain.property.PropertyAttributeValues;
import com.ryuqq.otatoy.domain.property.PropertyPhotos;
import com.ryuqq.otatoy.domain.roomtype.RoomTypes;

/**
 * 숙소 상세 조회 결과 DTO.
 * Property 기본 정보 + 사진 + 편의시설 + 속성값 + 객실 목록을 포함한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record PropertyDetail(
        Property property,
        PropertyPhotos photos,
        PropertyAmenities amenities,
        PropertyAttributeValues attributeValues,
        RoomTypes roomTypes
) {

    public static PropertyDetail of(Property property,
                                     PropertyPhotos photos,
                                     PropertyAmenities amenities,
                                     PropertyAttributeValues attributeValues,
                                     RoomTypes roomTypes) {
        return new PropertyDetail(property, photos, amenities, attributeValues, roomTypes);
    }
}
