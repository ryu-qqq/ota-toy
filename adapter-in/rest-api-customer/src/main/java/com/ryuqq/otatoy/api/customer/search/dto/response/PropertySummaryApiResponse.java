package com.ryuqq.otatoy.api.customer.search.dto.response;

import java.math.BigDecimal;

/**
 * 숙소 검색 결과 단건 응답 DTO.
 * 숙소 기본 정보 + 최저 가격을 포함한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record PropertySummaryApiResponse(
        Long propertyId,
        String name,
        Long propertyTypeId,
        String address,
        double latitude,
        double longitude,
        String region,
        BigDecimal lowestPrice
) {
}
