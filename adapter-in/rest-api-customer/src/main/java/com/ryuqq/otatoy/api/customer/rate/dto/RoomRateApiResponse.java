package com.ryuqq.otatoy.api.customer.rate.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 객실별 요금 응답 DTO.
 * 객실 유형 정보 + 요금 정책 + 날짜별 요금 + 총 금액을 포함한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record RoomRateApiResponse(
        Long roomTypeId,
        String roomTypeName,
        int maxOccupancy,
        Long ratePlanId,
        String ratePlanName,
        boolean freeCancellation,
        boolean nonRefundable,
        int cancellationDeadlineDays,
        List<DailyRateApiResponse> dailyRates,
        BigDecimal totalPrice
) {
}
