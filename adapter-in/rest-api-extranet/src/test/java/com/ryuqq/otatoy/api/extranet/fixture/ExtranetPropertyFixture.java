package com.ryuqq.otatoy.api.extranet.fixture;

import com.ryuqq.otatoy.application.property.dto.result.ExtranetPropertySliceResult;
import com.ryuqq.otatoy.application.property.dto.result.PropertyDetail;
import com.ryuqq.otatoy.application.property.dto.result.PropertySummary;
import com.ryuqq.otatoy.domain.accommodation.AmenityName;
import com.ryuqq.otatoy.domain.accommodation.AmenityType;
import com.ryuqq.otatoy.domain.accommodation.PhotoType;
import com.ryuqq.otatoy.domain.common.query.SliceMeta;
import com.ryuqq.otatoy.domain.common.vo.CdnUrl;
import com.ryuqq.otatoy.domain.common.vo.DeletionStatus;
import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.common.vo.OriginUrl;
import com.ryuqq.otatoy.domain.property.PropertyAmenities;
import com.ryuqq.otatoy.domain.property.PropertyAmenity;
import com.ryuqq.otatoy.domain.property.PropertyAmenityId;
import com.ryuqq.otatoy.domain.property.PropertyAttributeValue;
import com.ryuqq.otatoy.domain.property.PropertyAttributeValueId;
import com.ryuqq.otatoy.domain.property.PropertyAttributeValues;
import com.ryuqq.otatoy.domain.property.PropertyFixture;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.property.PropertyPhoto;
import com.ryuqq.otatoy.domain.property.PropertyPhotoId;
import com.ryuqq.otatoy.domain.property.PropertyPhotos;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeAttributeId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeFixture;
import com.ryuqq.otatoy.domain.roomtype.RoomTypes;

import java.time.Instant;
import java.util.List;

/**
 * Extranet Property API 테스트용 Fixture.
 * 요청 JSON과 Mock 응답 데이터를 중앙에서 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public final class ExtranetPropertyFixture {

    private ExtranetPropertyFixture() {}

    private static final Instant NOW = Instant.parse("2026-04-04T00:00:00Z");

    // === 요청 JSON ===

    /**
     * 숙소 등록 전체 필드 요청 JSON
     */
    public static String registerPropertyRequest() {
        return """
            {
                "partnerId": 1,
                "brandId": 1,
                "propertyTypeId": 1,
                "name": "테스트 호텔",
                "description": "좋은 호텔입니다",
                "address": "서울시 강남구 테헤란로 123",
                "latitude": 37.5665,
                "longitude": 126.978,
                "neighborhood": "강남",
                "region": "서울",
                "promotionText": "특가 프로모션"
            }
            """;
    }

    /**
     * 숙소 등록 최소 필수 필드만 포함한 요청 JSON
     */
    public static String registerPropertyMinimalRequest() {
        return """
            {
                "partnerId": 1,
                "propertyTypeId": 1,
                "name": "최소 호텔",
                "address": "서울시 강남구",
                "latitude": 37.5665,
                "longitude": 126.978
            }
            """;
    }

    /**
     * 숙소 등록 시 필수 필드 누락 요청 JSON (name 누락)
     */
    public static String registerPropertyInvalidRequest() {
        return """
            {
                "partnerId": 1,
                "propertyTypeId": 1,
                "address": "서울시 강남구",
                "latitude": 37.5665,
                "longitude": 126.978
            }
            """;
    }

    /**
     * 사진 설정 요청 JSON
     */
    public static String setPhotosRequest() {
        return """
            {
                "photos": [
                    {
                        "photoType": "EXTERIOR",
                        "originUrl": "https://example.com/exterior.jpg",
                        "cdnUrl": "https://cdn.example.com/exterior.jpg",
                        "sortOrder": 1
                    },
                    {
                        "photoType": "LOBBY",
                        "originUrl": "https://example.com/lobby.jpg",
                        "sortOrder": 2
                    }
                ]
            }
            """;
    }

    /**
     * 사진 설정 시 빈 목록 요청 JSON (유효성 검증 실패)
     */
    public static String setPhotosEmptyRequest() {
        return """
            {
                "photos": []
            }
            """;
    }

    /**
     * 편의시설 설정 요청 JSON
     */
    public static String setAmenitiesRequest() {
        return """
            {
                "amenities": [
                    {
                        "amenityType": "PARKING",
                        "name": "주차장",
                        "additionalPrice": 0,
                        "sortOrder": 1
                    },
                    {
                        "amenityType": "POOL",
                        "name": "수영장",
                        "additionalPrice": 15000,
                        "sortOrder": 2
                    }
                ]
            }
            """;
    }

    /**
     * 편의시설 설정 시 null 목록 요청 JSON (유효성 검증 실패)
     */
    public static String setAmenitiesInvalidRequest() {
        return """
            {
            }
            """;
    }

    /**
     * 속성값 설정 요청 JSON
     */
    public static String setAttributesRequest() {
        return """
            {
                "attributes": [
                    {
                        "propertyTypeAttributeId": 1,
                        "value": "5성"
                    },
                    {
                        "propertyTypeAttributeId": 2,
                        "value": "2020"
                    }
                ]
            }
            """;
    }

    /**
     * 속성값 설정 시 빈 목록 요청 JSON (유효성 검증 실패)
     */
    public static String setAttributesEmptyRequest() {
        return """
            {
                "attributes": []
            }
            """;
    }

    /**
     * 숙소 등록 시 name이 정확히 100자인 요청 JSON (경계값 성공)
     */
    public static String registerPropertyName100Request() {
        String name100 = "가".repeat(100);
        return """
            {
                "partnerId": 1,
                "propertyTypeId": 1,
                "name": "%s",
                "address": "서울시 강남구",
                "latitude": 37.5665,
                "longitude": 126.978
            }
            """.formatted(name100);
    }

    /**
     * 숙소 등록 시 name이 101자인 요청 JSON (경계값 실패)
     */
    public static String registerPropertyName101Request() {
        String name101 = "가".repeat(101);
        return """
            {
                "partnerId": 1,
                "propertyTypeId": 1,
                "name": "%s",
                "address": "서울시 강남구",
                "latitude": 37.5665,
                "longitude": 126.978
            }
            """.formatted(name101);
    }

    /**
     * 사진 설정 시 sortOrder 음수 요청 JSON (유효성 검증 실패)
     */
    public static String setPhotosSortOrderNegativeRequest() {
        return """
            {
                "photos": [
                    {
                        "photoType": "EXTERIOR",
                        "originUrl": "https://example.com/exterior.jpg",
                        "sortOrder": -1
                    }
                ]
            }
            """;
    }

    /**
     * 편의시설 설정 시 additionalPrice 음수 요청 JSON (유효성 검증 실패)
     */
    public static String setAmenitiesNegativePriceRequest() {
        return """
            {
                "amenities": [
                    {
                        "amenityType": "PARKING",
                        "name": "주차장",
                        "additionalPrice": -1000,
                        "sortOrder": 1
                    }
                ]
            }
            """;
    }

    /**
     * 속성값 설정 시 value가 빈 문자열인 요청 JSON (유효성 검증 실패)
     */
    public static String setAttributesBlankValueRequest() {
        return """
            {
                "attributes": [
                    {
                        "propertyTypeAttributeId": 1,
                        "value": ""
                    }
                ]
            }
            """;
    }

    // === Mock 응답 데이터 ===

    /**
     * 숙소 목록 조회 결과 (2건, 다음 페이지 있음)
     */
    public static ExtranetPropertySliceResult sliceResult() {
        var property1 = PropertyFixture.reconstitutedPropertyWithId(1L);
        var property2 = PropertyFixture.reconstitutedPropertyWithId(2L);

        List<PropertySummary> content = List.of(
            PropertySummary.of(property1, Money.of(80000)),
            PropertySummary.of(property2, Money.of(120000))
        );

        return ExtranetPropertySliceResult.of(content, new SliceMeta(true, 2L));
    }

    /**
     * 빈 숙소 목록 결과
     */
    public static ExtranetPropertySliceResult emptySliceResult() {
        return ExtranetPropertySliceResult.empty();
    }

    /**
     * 숙소 상세 조회 결과
     */
    public static PropertyDetail propertyDetail() {
        var property = PropertyFixture.reconstitutedProperty();

        PropertyPhotos photos = PropertyPhotos.forNew(List.of(
            PropertyPhoto.reconstitute(
                PropertyPhotoId.of(1L), PropertyId.of(1L), PhotoType.EXTERIOR,
                OriginUrl.of("https://example.com/exterior.jpg"),
                CdnUrl.of("https://cdn.example.com/exterior.jpg"),
                1, NOW, NOW, DeletionStatus.active()
            )
        ));

        PropertyAmenities amenities = PropertyAmenities.forNew(List.of(
            PropertyAmenity.reconstitute(
                PropertyAmenityId.of(1L), PropertyId.of(1L), AmenityType.PARKING,
                AmenityName.of("주차장"), Money.of(0), 1, NOW, NOW, DeletionStatus.active()
            )
        ));

        PropertyAttributeValues attributeValues = PropertyAttributeValues.forNew(List.of(
            PropertyAttributeValue.reconstitute(
                PropertyAttributeValueId.of(1L), PropertyId.of(1L),
                PropertyTypeAttributeId.of(1L), "5성",
                NOW, NOW, DeletionStatus.active()
            )
        ));

        RoomTypes roomTypes = RoomTypes.from(List.of(
            RoomTypeFixture.reconstitutedRoomType()
        ));

        return PropertyDetail.of(property, photos, amenities, attributeValues, roomTypes);
    }
}
