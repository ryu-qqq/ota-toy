package com.ryuqq.otatoy.domain.supplier;

/**
 * 공급자 동기화 로그 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public record SupplierSyncLogId(Long value) {

    public static SupplierSyncLogId of(Long value) {
        return new SupplierSyncLogId(value);
    }

    public static SupplierSyncLogId forNew() { return new SupplierSyncLogId(null); }

    public boolean isNew() {
        return value == null;
    }
}
