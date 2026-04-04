package com.ryuqq.otatoy.domain.supplier;

import com.ryuqq.otatoy.domain.accommodation.RoomTypeId;

import java.time.Instant;
import java.util.Objects;

public class SupplierRoomType {

    private final Long id;
    private final Long supplierPropertyId;
    private final RoomTypeId roomTypeId;
    private final String supplierRoomId;
    private Instant lastSyncedAt;
    private SupplierPropertyStatus status;

    private SupplierRoomType(Long id, Long supplierPropertyId, RoomTypeId roomTypeId,
                             String supplierRoomId, Instant lastSyncedAt,
                             SupplierPropertyStatus status) {
        this.id = id;
        this.supplierPropertyId = supplierPropertyId;
        this.roomTypeId = roomTypeId;
        this.supplierRoomId = supplierRoomId;
        this.lastSyncedAt = lastSyncedAt;
        this.status = status;
    }

    public static SupplierRoomType forNew(Long supplierPropertyId, RoomTypeId roomTypeId,
                                           String supplierRoomId) {
        if (supplierRoomId == null || supplierRoomId.isBlank()) {
            throw new IllegalArgumentException("공급자 객실 ID는 필수입니다");
        }
        return new SupplierRoomType(null, supplierPropertyId, roomTypeId, supplierRoomId,
                null, SupplierPropertyStatus.MAPPED);
    }

    public static SupplierRoomType reconstitute(Long id, Long supplierPropertyId, RoomTypeId roomTypeId,
                                                 String supplierRoomId, Instant lastSyncedAt,
                                                 SupplierPropertyStatus status) {
        return new SupplierRoomType(id, supplierPropertyId, roomTypeId, supplierRoomId, lastSyncedAt, status);
    }

    public void synced(Instant syncedAt) {
        this.lastSyncedAt = syncedAt;
    }

    public Long id() { return id; }
    public Long supplierPropertyId() { return supplierPropertyId; }
    public RoomTypeId roomTypeId() { return roomTypeId; }
    public String supplierRoomId() { return supplierRoomId; }
    public Instant lastSyncedAt() { return lastSyncedAt; }
    public SupplierPropertyStatus status() { return status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SupplierRoomType s)) return false;
        return id != null && id.equals(s.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
