package com.ryuqq.otatoy.persistence.supplier.entity;

import com.ryuqq.otatoy.persistence.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "supplier_room_type")
public class SupplierRoomTypeJpaEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long supplierPropertyId;

    @Column(nullable = false)
    private Long roomTypeId;

    @Column(nullable = false)
    private String supplierRoomCode;

    private Instant lastSyncedAt;

    @Column(nullable = false, length = 20)
    private String status;

    protected SupplierRoomTypeJpaEntity() {
        super();
    }

    public Long getId() { return id; }
    public Long getSupplierPropertyId() { return supplierPropertyId; }
    public Long getRoomTypeId() { return roomTypeId; }
    public String getSupplierRoomCode() { return supplierRoomCode; }
    public Instant getLastSyncedAt() { return lastSyncedAt; }
    public String getStatus() { return status; }
}
