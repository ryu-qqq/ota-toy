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

    private SupplierProperty(SupplierPropertyId id, SupplierId supplierId, PropertyId propertyId,
                             String supplierPropertyCode, Instant lastSyncedAt,
                             SupplierMappingStatus status) {
        this.id = id;
        this.supplierId = supplierId;
        this.propertyId = propertyId;
        this.supplierPropertyCode = supplierPropertyCode;
        this.lastSyncedAt = lastSyncedAt;
        this.status = status;
    }

    public static SupplierProperty forNew(SupplierId supplierId, PropertyId propertyId,
                                           String supplierPropertyCode) {
        validate(supplierPropertyCode);
        return new SupplierProperty(SupplierPropertyId.of(null), supplierId, propertyId, supplierPropertyCode,
                null, SupplierMappingStatus.MAPPED);
    }

    private static void validate(String supplierPropertyCode) {
        if (supplierPropertyCode == null || supplierPropertyCode.isBlank()) {
            throw new IllegalArgumentException("공급자 숙소 코드는 필수입니다");
        }
    }

    public static SupplierProperty reconstitute(SupplierPropertyId id, SupplierId supplierId, PropertyId propertyId,
                                                 String supplierPropertyCode, Instant lastSyncedAt,
                                                 SupplierMappingStatus status) {
        return new SupplierProperty(id, supplierId, propertyId, supplierPropertyCode, lastSyncedAt, status);
    }

    public void synced(Instant syncedAt) {
        if (this.status == SupplierMappingStatus.UNMAPPED) {
            throw new IllegalStateException("매핑 해제된 상태에서는 동기화할 수 없습니다");
        }
        this.lastSyncedAt = syncedAt;
    }

    public void unmap() {
        this.status = SupplierMappingStatus.UNMAPPED;
    }

    public SupplierPropertyId id() { return id; }
    public SupplierId supplierId() { return supplierId; }
    public PropertyId propertyId() { return propertyId; }
    public String supplierPropertyCode() { return supplierPropertyCode; }
    public Instant lastSyncedAt() { return lastSyncedAt; }
    public SupplierMappingStatus status() { return status; }

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
