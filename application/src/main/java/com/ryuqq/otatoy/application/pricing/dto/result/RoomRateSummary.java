package com.ryuqq.otatoy.application.pricing.dto.result;

import com.ryuqq.otatoy.domain.pricing.CancellationPolicy;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.domain.pricing.RatePlanName;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeName;

import java.math.BigDecimal;
import java.util.List;

/**
 * 객실별 요금 요약 정보.
 * 객실 유형 정보 + 요금 정책 + 날짜별 요금 목록 + 총 금액을 포함한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record RoomRateSummary(
        RoomTypeId roomTypeId,
        RoomTypeName roomTypeName,
        int maxOccupancy,
        RatePlanId ratePlanId,
        RatePlanName ratePlanName,
        CancellationPolicy cancellationPolicy,
        List<DailyRate> dailyRates,
        BigDecimal totalPrice
) {

    public static RoomRateSummary of(RoomTypeId roomTypeId, RoomTypeName roomTypeName,
                                      int maxOccupancy,
                                      RatePlanId ratePlanId, RatePlanName ratePlanName,
                                      CancellationPolicy cancellationPolicy,
                                      List<DailyRate> dailyRates, BigDecimal totalPrice) {
        return new RoomRateSummary(roomTypeId, roomTypeName, maxOccupancy,
                ratePlanId, ratePlanName, cancellationPolicy, dailyRates, totalPrice);
    }
}
