package com.ryuqq.otatoy.application.pricing.dto.result;

import com.ryuqq.otatoy.domain.property.PropertyId;

import java.util.List;

/**
 * 고객 요금 조회 전용 결과.
 * 제네릭 래퍼 대신 전용 Result로 래핑하여 확장성을 확보한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record CustomerPropertyRateResult(
        PropertyId propertyId,
        List<RoomRateSummary> roomRates
) {

    public CustomerPropertyRateResult {
        if (roomRates == null) {
            roomRates = List.of();
        }
    }

    public static CustomerPropertyRateResult of(PropertyId propertyId, List<RoomRateSummary> roomRates) {
        return new CustomerPropertyRateResult(propertyId, roomRates);
    }

    public static CustomerPropertyRateResult empty(PropertyId propertyId) {
        return new CustomerPropertyRateResult(propertyId, List.of());
    }
}
