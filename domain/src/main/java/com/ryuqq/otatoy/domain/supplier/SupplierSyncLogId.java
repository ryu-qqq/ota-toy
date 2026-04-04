package com.ryuqq.otatoy.domain.supplier;

public record SupplierSyncLogId(Long value) {

    public static SupplierSyncLogId of(Long value) {
        return new SupplierSyncLogId(value);
    }

    public boolean isNew() {
        return value == null;
    }
}
