package com.ryuqq.otatoy.api.customer.fixture;

import com.ryuqq.otatoy.application.pricing.dto.result.CustomerPropertyRateResult;
import com.ryuqq.otatoy.application.pricing.dto.result.DailyRate;
import com.ryuqq.otatoy.application.pricing.dto.result.RoomRateSummary;
import com.ryuqq.otatoy.domain.pricing.CancellationPolicy;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.domain.pricing.RatePlanName;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Customer 요금 조회 API 테스트용 Fixture.
 * 요청 파라미터와 Mock 응답 데이터를 중앙에서 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public final class CustomerRateFixture {

    private CustomerRateFixture() {}

    // === 요청 파라미터 상수 ===

    public static final Long PROPERTY_ID = 1L;
    public static final String CHECK_IN = "2026-05-01";
    public static final String CHECK_OUT = "2026-05-02";
    public static final String GUESTS = "2";

    // === Mock 응답 데이터 ===

    /**
     * 객실 요금 조회 결과 (1개 객실, 1박)
     */
    public static CustomerPropertyRateResult rateResult() {
        DailyRate dailyRate = DailyRate.of(
            LocalDate.of(2026, 5, 1), BigDecimal.valueOf(120000), 5, true);

        RoomRateSummary roomRate = RoomRateSummary.of(
            RoomTypeId.of(1L), RoomTypeName.of("디럭스 더블"), 2,
            RatePlanId.of(1L), RatePlanName.of("기본 요금"),
            CancellationPolicy.of(true, false, 3, "3일 전 무료 취소"),
            List.of(dailyRate), BigDecimal.valueOf(120000));

        return CustomerPropertyRateResult.of(PropertyId.of(PROPERTY_ID), List.of(roomRate));
    }

    /**
     * 빈 요금 결과
     */
    public static CustomerPropertyRateResult emptyRateResult() {
        return CustomerPropertyRateResult.empty(PropertyId.of(PROPERTY_ID));
    }
}
