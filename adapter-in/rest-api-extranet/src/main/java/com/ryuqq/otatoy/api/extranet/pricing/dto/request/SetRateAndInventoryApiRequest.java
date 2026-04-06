package com.ryuqq.otatoy.api.extranet.pricing.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 요금/재고 일괄 설정 요청 DTO.
 * RatePlan에 대한 RateRule(요일별 요금), RateOverride(날짜별 오버라이드),
 * Rate 스냅샷, Inventory를 일괄 설정한다.
 * <p>
 * 원시 타입으로 수신하며, {@link com.ryuqq.otatoy.api.extranet.pricing.mapper.RateAndInventoryApiMapper}에서
 * Application Command로 변환한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public record SetRateAndInventoryApiRequest(

    @NotNull(message = "시작일은 필수입니다")
    LocalDate startDate,

    @NotNull(message = "종료일은 필수입니다")
    LocalDate endDate,

    @NotNull(message = "기본 요금은 필수입니다")
    BigDecimal basePrice,

    BigDecimal weekdayPrice,

    BigDecimal fridayPrice,

    BigDecimal saturdayPrice,

    BigDecimal sundayPrice,

    @Min(value = 0, message = "기본 재고는 0 이상이어야 합니다")
    int baseInventory,

    List<@Valid OverrideItem> overrides
) {

    /**
     * 특정 날짜의 요금 오버라이드 항목.
     */
    public record OverrideItem(

        @NotNull(message = "오버라이드 날짜는 필수입니다")
        LocalDate date,

        @NotNull(message = "오버라이드 요금은 필수입니다")
        BigDecimal price,

        String reason
    ) {}
}
