package com.ryuqq.otatoy.application.pricing.dto.query;

import com.ryuqq.otatoy.domain.property.PropertyId;

import java.time.LocalDate;

/**
 * 고객 요금 조회 UseCase 입력 DTO.
 * Controller(ApiMapper)에서 변환하여 전달한다 (APP-DTO-001).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record CustomerGetRateQuery(
        PropertyId propertyId,
        LocalDate checkIn,
        LocalDate checkOut,
        int guests
) {
}
