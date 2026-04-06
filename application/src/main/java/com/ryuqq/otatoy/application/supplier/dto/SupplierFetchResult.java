package com.ryuqq.otatoy.application.supplier.dto;

import com.ryuqq.otatoy.domain.supplier.SupplierId;

/**
 * 외부 공급자 API 호출 결과 DTO.
 * Raw JSON 페이로드를 그대로 전달한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record SupplierFetchResult(
        SupplierId supplierId,
        String rawPayload
) {
}
