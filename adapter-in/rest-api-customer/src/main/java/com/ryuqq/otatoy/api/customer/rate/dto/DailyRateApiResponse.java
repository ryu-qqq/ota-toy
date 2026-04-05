package com.ryuqq.otatoy.api.customer.rate.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 날짜별 요금 응답 DTO.
 * 특정 날짜의 가격, 재고 가용 여부를 포함한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record DailyRateApiResponse(
        LocalDate date,
        BigDecimal basePrice,
        int availableCount,
        boolean available
) {
}
