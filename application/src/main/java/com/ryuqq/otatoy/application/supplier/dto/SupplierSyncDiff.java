package com.ryuqq.otatoy.application.supplier.dto;

import com.ryuqq.otatoy.domain.supplier.SupplierProperty;

import java.time.Instant;
import java.util.List;

/**
 * 공급자 동기화 Diff 결과.
 * 신규(added), 수정(updated), 삭제(deleted) 목록을 포함한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record SupplierSyncDiff(
        List<SupplierPropertyData> added,
        List<SupplierPropertyData> updated,
        List<SupplierProperty> deleted,
        Instant occurredAt
) {

    public int totalCount() {
        return added.size() + updated.size() + deleted.size();
    }
}
