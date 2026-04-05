package com.ryuqq.otatoy.application.pricing.dto.result;

import com.ryuqq.otatoy.domain.property.PropertyId;

import java.util.List;

/**
 * 숙소 요금 조회 결과.
 * 숙소 ID + 객실별 요금 요약 목록을 포함한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record PropertyRateResult(
        PropertyId propertyId,
        List<RoomRateSummary> roomRates
) {

    public static PropertyRateResult of(PropertyId propertyId, List<RoomRateSummary> roomRates) {
        return new PropertyRateResult(propertyId, roomRates);
    }
}
