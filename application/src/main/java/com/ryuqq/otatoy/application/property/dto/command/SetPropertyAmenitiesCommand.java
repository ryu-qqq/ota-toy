package com.ryuqq.otatoy.application.property.dto.command;

import com.ryuqq.otatoy.domain.accommodation.AmenityName;
import com.ryuqq.otatoy.domain.accommodation.AmenityType;
import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.property.PropertyId;

import java.util.List;

/**
 * 숙소 편의시설 설정 요청 Command.
 * 필드 타입은 Domain VO를 사용한다 (APP-DTO-001).
 * propertyId + List&lt;AmenityItem&gt; 구조.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public record SetPropertyAmenitiesCommand(
    PropertyId propertyId,
    List<AmenityItem> amenities
) {

    public static SetPropertyAmenitiesCommand of(PropertyId propertyId, List<AmenityItem> amenities) {
        return new SetPropertyAmenitiesCommand(propertyId, amenities != null ? amenities : List.of());
    }

    /**
     * 개별 편의시설 항목.
     */
    public record AmenityItem(
        AmenityType amenityType,
        AmenityName name,
        Money additionalPrice,
        int sortOrder
    ) {
        public static AmenityItem of(AmenityType amenityType, AmenityName name,
                                      Money additionalPrice, int sortOrder) {
            return new AmenityItem(amenityType, name, additionalPrice, sortOrder);
        }
    }
}
