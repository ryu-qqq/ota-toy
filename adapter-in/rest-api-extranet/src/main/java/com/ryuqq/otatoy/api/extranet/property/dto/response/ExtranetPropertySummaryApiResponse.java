package com.ryuqq.otatoy.api.extranet.property.dto.response;

import java.math.BigDecimal;

/**
 * Extranet 숙소 목록 조회 응답 단건 DTO.
 * 도메인 객체를 외부에 노출하지 않고 원시 타입으로 변환한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record ExtranetPropertySummaryApiResponse(
        Long propertyId,
        String name,
        Long propertyTypeId,
        String address,
        double latitude,
        double longitude,
        String neighborhood,
        String region,
        String status,
        BigDecimal lowestPrice,
        String createdAt,
        String updatedAt
) {}
