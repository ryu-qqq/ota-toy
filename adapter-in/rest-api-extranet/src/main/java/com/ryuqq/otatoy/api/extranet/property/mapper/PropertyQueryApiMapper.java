package com.ryuqq.otatoy.api.extranet.property.mapper;

import com.ryuqq.otatoy.api.core.DateTimeFormatUtils;
import com.ryuqq.otatoy.api.extranet.property.dto.response.ExtranetPropertyDetailApiResponse;
import com.ryuqq.otatoy.api.extranet.property.dto.response.ExtranetPropertyDetailApiResponse.AmenityApiResponse;
import com.ryuqq.otatoy.api.extranet.property.dto.response.ExtranetPropertyDetailApiResponse.AttributeValueApiResponse;
import com.ryuqq.otatoy.api.extranet.property.dto.response.ExtranetPropertyDetailApiResponse.PhotoApiResponse;
import com.ryuqq.otatoy.api.extranet.property.dto.response.ExtranetPropertyDetailApiResponse.RoomTypeSummaryApiResponse;
import com.ryuqq.otatoy.api.extranet.property.dto.response.ExtranetPropertySummaryApiResponse;
import com.ryuqq.otatoy.application.property.dto.query.ExtranetSearchPropertyQuery;
import com.ryuqq.otatoy.application.property.dto.result.PropertyDetail;
import com.ryuqq.otatoy.application.property.dto.result.PropertySummary;
import com.ryuqq.otatoy.domain.partner.PartnerId;
import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.roomtype.RoomType;

import java.util.List;

/**
 * 숙소 조회 API 요청/응답 변환 매퍼.
 * 요청: 원시 타입 → Domain VO
 * 응답: Application Result / Domain → ApiResponse DTO
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public final class PropertyQueryApiMapper {

    private PropertyQueryApiMapper() {}

    /**
     * 파트너 숙소 목록 조회 API 파라미터를 ExtranetSearchPropertyQuery로 변환한다.
     */
    public static ExtranetSearchPropertyQuery toExtranetSearchQuery(Long partnerId, int size, Long cursor) {
        return new ExtranetSearchPropertyQuery(
            PartnerId.of(partnerId),
            size,
            cursor
        );
    }

    /**
     * 숙소 ID 원시값을 PropertyId VO로 변환한다.
     */
    public static PropertyId toPropertyId(Long propertyId) {
        return PropertyId.of(propertyId);
    }

    /**
     * PropertySummary → 외부 응답 DTO로 변환한다.
     * ID VO, Money VO 등 도메인 타입을 원시 타입으로 풀어서 반환한다.
     */
    public static ExtranetPropertySummaryApiResponse toSummaryResponse(PropertySummary summary) {
        return new ExtranetPropertySummaryApiResponse(
                summary.propertyId().value(),
                summary.name().value(),
                summary.propertyTypeId().value(),
                summary.location().address(),
                summary.location().latitude(),
                summary.location().longitude(),
                summary.location().neighborhood(),
                summary.location().region(),
                null,
                summary.lowestPrice() != null ? summary.lowestPrice().amount() : null,
                null,
                null
        );
    }

    /**
     * PropertyDetail → 외부 응답 DTO로 변환한다.
     * 도메인 객체 그래프를 평탄화하여 원시 타입만 포함하는 응답을 구성한다.
     */
    public static ExtranetPropertyDetailApiResponse toDetailResponse(PropertyDetail detail) {
        Property p = detail.property();

        List<PhotoApiResponse> photos = detail.photos().stream()
                .map(photo -> new PhotoApiResponse(
                        photo.id().value(),
                        photo.photoType().name(),
                        photo.originUrl().value(),
                        photo.cdnUrl() != null ? photo.cdnUrl().value() : null,
                        photo.sortOrder()))
                .toList();

        List<AmenityApiResponse> amenities = detail.amenities().stream()
                .map(amenity -> new AmenityApiResponse(
                        amenity.id().value(),
                        amenity.amenityType().name(),
                        amenity.name().value(),
                        amenity.additionalPrice() != null ? amenity.additionalPrice().toPlainString() : "0",
                        amenity.sortOrder()))
                .toList();

        List<AttributeValueApiResponse> attributeValues = detail.attributeValues().stream()
                .map(attr -> new AttributeValueApiResponse(
                        attr.id().value(),
                        attr.propertyTypeAttributeId().value(),
                        attr.value()))
                .toList();

        List<RoomTypeSummaryApiResponse> roomTypes = detail.roomTypes().stream()
                .map(PropertyQueryApiMapper::toRoomTypeSummary)
                .toList();

        return new ExtranetPropertyDetailApiResponse(
                p.id().value(),
                p.partnerId().value(),
                p.brandId() != null ? p.brandId().value() : null,
                p.propertyTypeId().value(),
                p.name().value(),
                p.description() != null ? p.description().value() : null,
                p.location().address(),
                p.location().latitude(),
                p.location().longitude(),
                p.location().neighborhood(),
                p.location().region(),
                p.promotionText() != null ? p.promotionText().value() : null,
                p.status().name(),
                DateTimeFormatUtils.formatDateTime(p.createdAt()),
                DateTimeFormatUtils.formatDateTime(p.updatedAt()),
                photos,
                amenities,
                attributeValues,
                roomTypes
        );
    }

    private static RoomTypeSummaryApiResponse toRoomTypeSummary(RoomType rt) {
        return new RoomTypeSummaryApiResponse(
                rt.id().value(),
                rt.name().value(),
                rt.description() != null ? rt.description().value() : null,
                rt.areaSqm() != null ? rt.areaSqm().toPlainString() : null,
                rt.areaPyeong(),
                rt.baseOccupancy(),
                rt.maxOccupancy(),
                rt.baseInventory(),
                rt.checkInTime() != null ? rt.checkInTime().toString() : null,
                rt.checkOutTime() != null ? rt.checkOutTime().toString() : null,
                rt.status().name()
        );
    }

}
