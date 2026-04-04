package com.ryuqq.otatoy.domain.supplier;

import com.ryuqq.otatoy.domain.accommodation.PropertyId;

import java.time.Instant;
import java.util.Objects;

public class SupplierProperty {

    private final Long id;
    private final SupplierId supplierId;
    private final PropertyId propertyId;
    private final String supplierPropertyId;
    private Instant lastSyncedAt;
    private SupplierPropertyStatus status;

    private SupplierProperty(Long id, SupplierId supplierId, PropertyId propertyId,
                             String supplierPropertyId, Instant lastSyncedAt,
                             SupplierPropertyStatus status) {
        this.id = id;
        this.supplierId = supplierId;
        this.propertyId = propertyId;
        this.supplierPropertyId = supplierPropertyId;
        this.lastSyncedAt = lastSyncedAt;
        this.status = status;
    }

    public static SupplierProperty forNew(SupplierId supplierId, PropertyId propertyId,
                                           String supplierPropertyId) {
        if (supplierPropertyId == null || supplierPropertyId.isBlank()) {
            throw new IllegalArgumentException("공급자 숙소 ID는 필수입니다");
        }
        return new SupplierProperty(null, supplierId, propertyId, supplierPropertyId,
                null, SupplierPropertyStatus.MAPPED);
    }

    public static SupplierProperty reconstitute(Long id, SupplierId supplierId, PropertyId propertyId,
                                                 String supplierPropertyId, Instant lastSyncedAt,
                                                 SupplierPropertyStatus status) {
        return new SupplierProperty(id, supplierId, propertyId, supplierPropertyId, lastSyncedAt, status);
    }

    public void synced(Instant syncedAt) {
        this.lastSyncedAt = syncedAt;
    }

    public void unmap() {
        this.status = SupplierPropertyStatus.UNMAPPED;
    }

    public Long id() { return id; }
    public SupplierId supplierId() { return supplierId; }
    public PropertyId propertyId() { return propertyId; }
    public String supplierPropertyId() { return supplierPropertyId; }
    public Instant lastSyncedAt() { return lastSyncedAt; }
    public SupplierPropertyStatus status() { return status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SupplierProperty s)) return false;
        return id != null && id.equals(s.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
