package com.ryuqq.otatoy.api.extranet.property.mapper;

import com.ryuqq.otatoy.api.extranet.property.dto.SetPropertyAmenitiesApiRequest;
import com.ryuqq.otatoy.application.property.dto.command.SetPropertyAmenitiesCommand;
import com.ryuqq.otatoy.domain.accommodation.AmenityName;
import com.ryuqq.otatoy.domain.accommodation.AmenityType;
import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.property.PropertyId;

/**
 * 편의시설 API Request DTO를 Application Command로 변환하는 매퍼.
 * 원시 타입(String, BigDecimal)을 Domain VO로 변환하는 책임을 전담한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public final class PropertyAmenityApiMapper {

    private PropertyAmenityApiMapper() {}

    /**
     * 편의시설 설정 API 요청을 Application Command로 변환한다.
     *
     * @param propertyId 숙소 ID (PathVariable)
     * @param request    편의시설 설정 요청 DTO
     * @return 편의시설 설정 Command
     */
    public static SetPropertyAmenitiesCommand toCommand(Long propertyId, SetPropertyAmenitiesApiRequest request) {
        return SetPropertyAmenitiesCommand.of(
            PropertyId.of(propertyId),
            request.amenities().stream()
                .map(amenity -> SetPropertyAmenitiesCommand.AmenityItem.of(
                    AmenityType.valueOf(amenity.amenityType()),
                    AmenityName.of(amenity.name()),
                    Money.of(amenity.additionalPrice()),
                    amenity.sortOrder()
                ))
                .toList()
        );
    }
}
