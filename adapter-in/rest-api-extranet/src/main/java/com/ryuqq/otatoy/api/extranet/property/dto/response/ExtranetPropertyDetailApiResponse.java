package com.ryuqq.otatoy.api.extranet.property.dto.response;

import java.util.List;

/**
 * Extranet 숙소 상세 조회 응답 DTO.
 * Property 기본 정보 + 사진 + 편의시설 + 속성값 + 객실 목록을 포함한다.
 * 도메인 객체를 외부에 노출하지 않고 원시 타입으로 변환한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record ExtranetPropertyDetailApiResponse(
        Long propertyId,
        Long partnerId,
        Long brandId,
        Long propertyTypeId,
        String name,
        String description,
        String address,
        double latitude,
        double longitude,
        String neighborhood,
        String region,
        String promotionText,
        String status,
        String createdAt,
        String updatedAt,
        List<PhotoApiResponse> photos,
        List<AmenityApiResponse> amenities,
        List<AttributeValueApiResponse> attributeValues,
        List<RoomTypeSummaryApiResponse> roomTypes
) {

    public record PhotoApiResponse(
            Long id,
            String photoType,
            String originUrl,
            String cdnUrl,
            int sortOrder
    ) {}

    public record AmenityApiResponse(
            Long id,
            String amenityType,
            String name,
            String additionalPrice,
            int sortOrder
    ) {}

    public record AttributeValueApiResponse(
            Long id,
            Long propertyTypeAttributeId,
            String value
    ) {}

    public record RoomTypeSummaryApiResponse(
            Long roomTypeId,
            String name,
            String description,
            String areaSqm,
            String areaPyeong,
            int baseOccupancy,
            int maxOccupancy,
            int baseInventory,
            String checkInTime,
            String checkOutTime,
            String status
    ) {}
}
