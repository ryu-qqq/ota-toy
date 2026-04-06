package com.ryuqq.otatoy.application.supplier.dto;

import com.ryuqq.otatoy.domain.supplier.SupplierId;
import com.ryuqq.otatoy.domain.supplier.SupplierProperty;

import java.time.Instant;
import java.util.List;

/**
 * 공급자 동기화 Diff 결과.
 * 자기가 필요한 데이터를 모두 포함한다 — 외부에서 supplierId 등을 별도로 넘길 필요 없음.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record SupplierSyncDiff(
        SupplierId supplierId,
        List<SupplierPropertyData> added,
        List<SupplierPropertyData> updated,
        List<SupplierProperty> deleted,
        Instant occurredAt
) {

    public int totalCount() {
        return added.size() + updated.size() + deleted.size();
    }
}
