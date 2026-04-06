package com.ryuqq.otatoy.api.extranet.property.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

/**
 * 숙소 편의시설 설정 요청 DTO.
 * 전체 교체(replace) 방식으로 설정된다.
 * 원시 타입으로 수신하며, {@link com.ryuqq.otatoy.api.extranet.property.mapper.PropertyAmenityApiMapper}에서
 * Domain VO로 변환한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public record SetPropertyAmenitiesApiRequest(

    @NotNull(message = "편의시설 목록은 필수입니다")
    List<@Valid AmenityItem> amenities
) {

    /**
     * 개별 편의시설 항목.
     *
     * @param amenityType    편의시설 유형 코드 (예: GENERAL, BATHROOM)
     * @param name           편의시설 이름
     * @param additionalPrice 추가 요금 (0이면 무료)
     * @param sortOrder      정렬 순서 (0 이상)
     */
    public record AmenityItem(

        @NotBlank(message = "편의시설 유형은 필수입니다")
        String amenityType,

        @NotBlank(message = "편의시설 이름은 필수입니다")
        String name,

        @NotNull(message = "추가 요금은 필수입니다")
        @PositiveOrZero(message = "추가 요금은 0 이상이어야 합니다")
        BigDecimal additionalPrice,

        @PositiveOrZero(message = "정렬 순서는 0 이상이어야 합니다")
        int sortOrder
    ) {}
}
