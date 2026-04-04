package com.ryuqq.otatoy.domain.supplier;

import com.ryuqq.otatoy.domain.accommodation.RoomTypeId;

import java.time.Instant;
import java.util.Objects;

public class SupplierRoomType {

    private final SupplierRoomTypeId id;
    private final SupplierPropertyId supplierPropertyId;
    private final RoomTypeId roomTypeId;
    private final String supplierRoomCode;
    private Instant lastSyncedAt;
    private SupplierMappingStatus status;

    private SupplierRoomType(SupplierRoomTypeId id, SupplierPropertyId supplierPropertyId, RoomTypeId roomTypeId,
                             String supplierRoomCode, Instant lastSyncedAt,
                             SupplierMappingStatus status) {
        this.id = id;
        this.supplierPropertyId = supplierPropertyId;
        this.roomTypeId = roomTypeId;
        this.supplierRoomCode = supplierRoomCode;
        this.lastSyncedAt = lastSyncedAt;
        this.status = status;
    }

    public static SupplierRoomType forNew(SupplierPropertyId supplierPropertyId, RoomTypeId roomTypeId,
                                           String supplierRoomCode) {
        validate(supplierRoomCode);
        return new SupplierRoomType(SupplierRoomTypeId.of(null), supplierPropertyId, roomTypeId, supplierRoomCode,
                null, SupplierMappingStatus.MAPPED);
    }

    private static void validate(String supplierRoomCode) {
        if (supplierRoomCode == null || supplierRoomCode.isBlank()) {
            throw new IllegalArgumentException("공급자 객실 코드는 필수입니다");
        }
    }

    public static SupplierRoomType reconstitute(SupplierRoomTypeId id, SupplierPropertyId supplierPropertyId, RoomTypeId roomTypeId,
                                                 String supplierRoomCode, Instant lastSyncedAt,
                                                 SupplierMappingStatus status) {
        return new SupplierRoomType(id, supplierPropertyId, roomTypeId, supplierRoomCode, lastSyncedAt, status);
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

    public SupplierRoomTypeId id() { return id; }
    public SupplierPropertyId supplierPropertyId() { return supplierPropertyId; }
    public RoomTypeId roomTypeId() { return roomTypeId; }
    public String supplierRoomCode() { return supplierRoomCode; }
    public Instant lastSyncedAt() { return lastSyncedAt; }
    public SupplierMappingStatus status() { return status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SupplierRoomType s)) return false;
        return id != null && id.value() != null && id.equals(s.id);
    }

    @Override
    public int hashCode() {
        return id != null && id.value() != null ? Objects.hashCode(id) : System.identityHashCode(this);
    }
}
