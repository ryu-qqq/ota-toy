package com.ryuqq.otatoy.application.pricing.dto.query;

import com.ryuqq.otatoy.domain.property.PropertyId;

import java.time.LocalDate;

/**
 * 요금 조회 UseCase 입력 DTO.
 * Controller(ApiMapper)에서 변환하여 전달한다 (APP-DTO-001).
 * 인스턴스 메서드 금지 -- 순수 데이터 컨테이너 역할만 한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record FetchRateQuery(
        PropertyId propertyId,
        LocalDate checkIn,
        LocalDate checkOut,
        int guests
) {
}
