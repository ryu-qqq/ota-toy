package com.ryuqq.otatoy.domain.supplier;

import com.ryuqq.otatoy.domain.accommodation.PropertyId;

import java.time.Instant;
import java.util.Objects;

public class SupplierProperty {

    private final SupplierPropertyId id;
    private final SupplierId supplierId;
    private final PropertyId propertyId;
    private final String supplierPropertyCode;
    private Instant lastSyncedAt;
    private SupplierPropertyStatus status;

    private SupplierProperty(SupplierPropertyId id, SupplierId supplierId, PropertyId propertyId,
                             String supplierPropertyCode, Instant lastSyncedAt,
                             SupplierPropertyStatus status) {
        this.id = id;
        this.supplierId = supplierId;
        this.propertyId = propertyId;
        this.supplierPropertyCode = supplierPropertyCode;
        this.lastSyncedAt = lastSyncedAt;
        this.status = status;
    }

    public static SupplierProperty forNew(SupplierId supplierId, PropertyId propertyId,
                                           String supplierPropertyCode) {
        if (supplierPropertyCode == null || supplierPropertyCode.isBlank()) {
            throw new IllegalArgumentException("공급자 숙소 코드는 필수입니다");
        }
        return new SupplierProperty(SupplierPropertyId.of(null), supplierId, propertyId, supplierPropertyCode,
                null, SupplierPropertyStatus.MAPPED);
    }

    public static SupplierProperty reconstitute(SupplierPropertyId id, SupplierId supplierId, PropertyId propertyId,
                                                 String supplierPropertyCode, Instant lastSyncedAt,
                                                 SupplierPropertyStatus status) {
        return new SupplierProperty(id, supplierId, propertyId, supplierPropertyCode, lastSyncedAt, status);
    }

    public void synced(Instant syncedAt) {
        this.lastSyncedAt = syncedAt;
    }

    public void unmap() {
        this.status = SupplierPropertyStatus.UNMAPPED;
    }

    public SupplierPropertyId id() { return id; }
    public SupplierId supplierId() { return supplierId; }
    public PropertyId propertyId() { return propertyId; }
    public String supplierPropertyCode() { return supplierPropertyCode; }
    public Instant lastSyncedAt() { return lastSyncedAt; }
    public SupplierPropertyStatus status() { return status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SupplierProperty s)) return false;
        return id != null && id.value() != null && id.equals(s.id);
    }

    @Override
    public int hashCode() {
        return id != null && id.value() != null ? Objects.hashCode(id) : System.identityHashCode(this);
    }
}
