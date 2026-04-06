package com.ryuqq.otatoy.application.supplier.dto;

import com.ryuqq.otatoy.domain.supplier.SupplierId;

import java.time.Instant;

/**
 * 외부 공급자 API 호출 결과 DTO.
 * Raw JSON 페이로드와 수집 시각을 전달한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record SupplierFetchResult(
        SupplierId supplierId,
        String rawPayload,
        Instant fetchedAt
) {
}
