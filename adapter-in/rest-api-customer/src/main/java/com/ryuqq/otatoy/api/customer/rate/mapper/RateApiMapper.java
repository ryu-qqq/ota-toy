package com.ryuqq.otatoy.api.customer.rate.mapper;

import com.ryuqq.otatoy.api.customer.rate.dto.response.DailyRateApiResponse;
import com.ryuqq.otatoy.api.customer.rate.dto.response.RoomRateApiResponse;
import com.ryuqq.otatoy.application.pricing.dto.query.CustomerGetRateQuery;
import com.ryuqq.otatoy.application.pricing.dto.result.DailyRate;
import com.ryuqq.otatoy.application.pricing.dto.result.RoomRateSummary;
import com.ryuqq.otatoy.domain.property.PropertyId;

import java.time.LocalDate;

/**
 * 요금 조회 API 변환 매퍼.
 * Request → Query, Result → Response 변환을 전담한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public final class RateApiMapper {

    private RateApiMapper() {
    }

    public static CustomerGetRateQuery toQuery(Long propertyId, LocalDate checkIn, LocalDate checkOut, int guests) {
        return new CustomerGetRateQuery(
                PropertyId.of(propertyId),
                checkIn,
                checkOut,
                guests
        );
    }

    public static RoomRateApiResponse toApiResponse(RoomRateSummary summary) {
        return new RoomRateApiResponse(
                summary.roomTypeId().value(),
                summary.roomTypeName().value(),
                summary.maxOccupancy(),
                summary.ratePlanId().value(),
                summary.ratePlanName().value(),
                summary.cancellationPolicy().freeCancellation(),
                summary.cancellationPolicy().nonRefundable(),
                summary.cancellationPolicy().deadlineDays(),
                summary.dailyRates().stream()
                        .map(RateApiMapper::toDailyRateResponse)
                        .toList(),
                summary.totalPrice()
        );
    }

    public static DailyRateApiResponse toDailyRateResponse(DailyRate dailyRate) {
        return new DailyRateApiResponse(
                dailyRate.date(),
                dailyRate.basePrice(),
                dailyRate.availableCount(),
                dailyRate.available()
        );
    }
}
