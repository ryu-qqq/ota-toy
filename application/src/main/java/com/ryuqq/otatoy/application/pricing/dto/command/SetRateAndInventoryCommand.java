package com.ryuqq.otatoy.application.pricing.dto.command;

import com.ryuqq.otatoy.domain.pricing.RatePlanId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 요금/재고 설정 커맨드.
 * RatePlan에 대한 RateRule + RateOverride + Rate + Inventory를 일괄 설정한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public record SetRateAndInventoryCommand(
    RatePlanId ratePlanId,
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal basePrice,
    BigDecimal weekdayPrice,
    BigDecimal fridayPrice,
    BigDecimal saturdayPrice,
    BigDecimal sundayPrice,
    int baseInventory,
    List<OverrideItem> overrides
) {

    /**
     * 특정 날짜의 요금 오버라이드 항목.
     */
    public record OverrideItem(
        LocalDate date,
        BigDecimal price,
        String reason
    ) {}
}
