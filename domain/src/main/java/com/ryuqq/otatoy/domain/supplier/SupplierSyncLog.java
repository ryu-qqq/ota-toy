package com.ryuqq.otatoy.domain.supplier;

import java.time.Instant;
import java.util.Objects;

public class SupplierSyncLog {

    private final SupplierSyncLogId id;
    private final SupplierId supplierId;
    private final SupplierSyncType syncType;
    private final Instant syncedAt;
    private SupplierSyncStatus status;
    private final int totalCount;
    private final int createdCount;
    private final int updatedCount;
    private final int deletedCount;
    private String errorMessage;

    private SupplierSyncLog(SupplierSyncLogId id, SupplierId supplierId, SupplierSyncType syncType, Instant syncedAt,
                            SupplierSyncStatus status, int totalCount, int createdCount,
                            int updatedCount, int deletedCount, String errorMessage) {
        this.id = id;
        this.supplierId = supplierId;
        this.syncType = syncType;
        this.syncedAt = syncedAt;
        this.status = status;
        this.totalCount = totalCount;
        this.createdCount = createdCount;
        this.updatedCount = updatedCount;
        this.deletedCount = deletedCount;
        this.errorMessage = errorMessage;
    }

    public static SupplierSyncLog forSuccess(SupplierId supplierId, SupplierSyncType syncType, Instant syncedAt,
                                              int totalCount, int createdCount, int updatedCount,
                                              int deletedCount) {
        return new SupplierSyncLog(SupplierSyncLogId.of(null), supplierId, syncType, syncedAt,
                SupplierSyncStatus.SUCCESS, totalCount, createdCount, updatedCount, deletedCount, null);
    }

    public static SupplierSyncLog forFailed(SupplierId supplierId, SupplierSyncType syncType, Instant syncedAt,
                                             String errorMessage) {
        return new SupplierSyncLog(SupplierSyncLogId.of(null), supplierId, syncType, syncedAt,
                SupplierSyncStatus.FAILED, 0, 0, 0, 0, errorMessage);
    }

    public static SupplierSyncLog reconstitute(SupplierSyncLogId id, SupplierId supplierId, SupplierSyncType syncType, Instant syncedAt,
                                                SupplierSyncStatus status, int totalCount, int createdCount,
                                                int updatedCount, int deletedCount, String errorMessage) {
        return new SupplierSyncLog(id, supplierId, syncType, syncedAt, status,
                totalCount, createdCount, updatedCount, deletedCount, errorMessage);
    }

    public void markFailed(String errorMessage) {
        this.status = SupplierSyncStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public SupplierSyncLogId id() { return id; }
    public SupplierId supplierId() { return supplierId; }
    public SupplierSyncType syncType() { return syncType; }
    public Instant syncedAt() { return syncedAt; }
    public SupplierSyncStatus status() { return status; }
    public int totalCount() { return totalCount; }
    public int createdCount() { return createdCount; }
    public int updatedCount() { return updatedCount; }
    public int deletedCount() { return deletedCount; }
    public String errorMessage() { return errorMessage; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SupplierSyncLog s)) return false;
        return id != null && id.value() != null && id.equals(s.id);
    }

    @Override
    public int hashCode() {
        return id != null && id.value() != null ? Objects.hashCode(id) : System.identityHashCode(this);
    }
}
