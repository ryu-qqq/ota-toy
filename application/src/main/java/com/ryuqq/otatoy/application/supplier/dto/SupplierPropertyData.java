package com.ryuqq.otatoy.application.supplier.dto;

import java.util.List;

/**
 * 외부 공급자로부터 변환된 숙소 데이터 DTO.
 * ACL 변환의 결과물이며, 내부 도메인 모델과는 독립적이다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record SupplierPropertyData(
        String externalPropertyId,
        String name,
        String description,
        String address,
        double latitude,
        double longitude,
        String propertyType,
        List<SupplierRoomData> rooms
) {
}
