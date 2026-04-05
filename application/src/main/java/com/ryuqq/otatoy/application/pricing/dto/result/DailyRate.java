package com.ryuqq.otatoy.application.pricing.dto.result;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 특정 날짜의 요금 정보.
 * 날짜, 기본 가격, 재고 가용 여부를 포함한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record DailyRate(
        LocalDate date,
        BigDecimal basePrice,
        int availableCount,
        boolean available
) {

    public static DailyRate of(LocalDate date, BigDecimal basePrice, int availableCount, boolean available) {
        return new DailyRate(date, basePrice, availableCount, available);
    }
}
