package com.ryuqq.otatoy.application.property.dto.result;

import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.property.Location;
import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.property.PropertyName;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;

/**
 * 숙소 검색 결과 단건 DTO.
 * 숙소 기본 정보 + 최저 가격을 포함한다 (STORY-201 AC-5).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record PropertySummary(
        PropertyId propertyId,
        PropertyName name,
        PropertyTypeId propertyTypeId,
        Location location,
        Money lowestPrice
) {

    public static PropertySummary of(Property property, Money lowestPrice) {
        return new PropertySummary(
                property.id(),
                property.name(),
                property.propertyTypeId(),
                property.location(),
                lowestPrice
        );
    }
}
