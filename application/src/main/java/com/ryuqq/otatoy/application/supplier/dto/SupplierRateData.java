package com.ryuqq.otatoy.application.supplier.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 외부 공급자로부터 변환된 요금/재고 데이터 DTO.
 * ACL 변환의 결과물이며, 내부 도메인 모델과는 독립적이다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record SupplierRateData(
        String externalRoomId,
        String ratePlanName,
        boolean freeCancellation,
        boolean nonRefundable,
        String paymentPolicy,
        List<SupplierDailyRate> dailyRates
) {

    /**
     * 특정 날짜의 가격 + 재고 정보.
     */
    public record SupplierDailyRate(
            LocalDate date,
            BigDecimal price,
            int availableCount,
            boolean stopSell
    ) {
    }
}
