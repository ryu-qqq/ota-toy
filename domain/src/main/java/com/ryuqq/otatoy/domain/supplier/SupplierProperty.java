package com.ryuqq.otatoy.domain.supplier;

import com.ryuqq.otatoy.domain.property.PropertyId;

import java.time.Instant;
import java.util.Objects;

/**
 * 공급자와 숙소 간의 매핑을 나타내는 엔티티.
 * 공급자 측 숙소 코드와 마지막 동기화 시각을 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public class SupplierProperty {

    private final SupplierPropertyId id;
    private final SupplierId supplierId;
    private final PropertyId propertyId;
    private final String supplierPropertyCode;
    private Instant lastSyncedAt;
    private SupplierMappingStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private SupplierProperty(SupplierPropertyId id, SupplierId supplierId, PropertyId propertyId,
                             String supplierPropertyCode, Instant lastSyncedAt,
                             SupplierMappingStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.supplierId = supplierId;
        this.propertyId = propertyId;
        this.supplierPropertyCode = supplierPropertyCode;
        this.lastSyncedAt = lastSyncedAt;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SupplierProperty forNew(SupplierId supplierId, PropertyId propertyId,
                                           String supplierPropertyCode, Instant now) {
        validate(supplierPropertyCode);
        return new SupplierProperty(SupplierPropertyId.of(null), supplierId, propertyId, supplierPropertyCode,
                null, SupplierMappingStatus.MAPPED, now, now);
    }

    private static void validate(String supplierPropertyCode) {
        if (supplierPropertyCode == null || supplierPropertyCode.isBlank()) {
            throw new IllegalArgumentException("공급자 숙소 코드는 필수입니다");
        }
    }

    public static SupplierProperty reconstitute(SupplierPropertyId id, SupplierId supplierId, PropertyId propertyId,
                                                 String supplierPropertyCode, Instant lastSyncedAt,
                                                 SupplierMappingStatus status, Instant createdAt, Instant updatedAt) {
        return new SupplierProperty(id, supplierId, propertyId, supplierPropertyCode, lastSyncedAt, status, createdAt, updatedAt);
    }

    public void synced(Instant syncedAt) {
        if (this.status == SupplierMappingStatus.UNMAPPED) {
            throw new IllegalStateException("매핑 해제된 상태에서는 동기화할 수 없습니다");
        }
        this.lastSyncedAt = syncedAt;
        this.updatedAt = syncedAt;
    }

    public void unmap(Instant now) {
        this.status = SupplierMappingStatus.UNMAPPED;
        this.updatedAt = now;
    }

    public SupplierPropertyId id() { return id; }
    public SupplierId supplierId() { return supplierId; }
    public PropertyId propertyId() { return propertyId; }
    public String supplierPropertyCode() { return supplierPropertyCode; }
    public Instant lastSyncedAt() { return lastSyncedAt; }
    public SupplierMappingStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

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
