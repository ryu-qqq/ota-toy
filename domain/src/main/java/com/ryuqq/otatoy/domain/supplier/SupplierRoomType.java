package com.ryuqq.otatoy.domain.supplier;

import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

import java.time.Instant;
import java.util.Objects;

/**
 * 공급자와 객실 유형 간의 매핑을 나타내는 엔티티.
 * 공급자 측 객실 코드와 마지막 동기화 시각을 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public class SupplierRoomType {

    private final SupplierRoomTypeId id;
    private final SupplierPropertyId supplierPropertyId;
    private final RoomTypeId roomTypeId;
    private final String supplierRoomCode;
    private Instant lastSyncedAt;
    private SupplierMappingStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private SupplierRoomType(SupplierRoomTypeId id, SupplierPropertyId supplierPropertyId, RoomTypeId roomTypeId,
                             String supplierRoomCode, Instant lastSyncedAt,
                             SupplierMappingStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.supplierPropertyId = supplierPropertyId;
        this.roomTypeId = roomTypeId;
        this.supplierRoomCode = supplierRoomCode;
        this.lastSyncedAt = lastSyncedAt;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SupplierRoomType forNew(SupplierPropertyId supplierPropertyId, RoomTypeId roomTypeId,
                                           String supplierRoomCode, Instant now) {
        validate(supplierRoomCode);
        return new SupplierRoomType(SupplierRoomTypeId.of(null), supplierPropertyId, roomTypeId, supplierRoomCode,
                null, SupplierMappingStatus.MAPPED, now, now);
    }

    private static void validate(String supplierRoomCode) {
        if (supplierRoomCode == null || supplierRoomCode.isBlank()) {
            throw new IllegalArgumentException("공급자 객실 코드는 필수입니다");
        }
    }

    public static SupplierRoomType reconstitute(SupplierRoomTypeId id, SupplierPropertyId supplierPropertyId, RoomTypeId roomTypeId,
                                                 String supplierRoomCode, Instant lastSyncedAt,
                                                 SupplierMappingStatus status, Instant createdAt, Instant updatedAt) {
        return new SupplierRoomType(id, supplierPropertyId, roomTypeId, supplierRoomCode, lastSyncedAt, status, createdAt, updatedAt);
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

    public SupplierRoomTypeId id() { return id; }
    public SupplierPropertyId supplierPropertyId() { return supplierPropertyId; }
    public RoomTypeId roomTypeId() { return roomTypeId; }
    public String supplierRoomCode() { return supplierRoomCode; }
    public Instant lastSyncedAt() { return lastSyncedAt; }
    public SupplierMappingStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

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
