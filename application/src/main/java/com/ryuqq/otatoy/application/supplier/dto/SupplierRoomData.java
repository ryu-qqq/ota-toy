package com.ryuqq.otatoy.application.supplier.dto;

import java.math.BigDecimal;

/**
 * 외부 공급자로부터 변환된 객실 데이터 DTO.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record SupplierRoomData(
        String externalRoomId,
        String name,
        int maxOccupancy,
        BigDecimal price
) {
}
