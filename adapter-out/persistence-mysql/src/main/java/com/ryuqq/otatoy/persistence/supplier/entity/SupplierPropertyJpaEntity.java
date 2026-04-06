package com.ryuqq.otatoy.persistence.supplier.entity;

import com.ryuqq.otatoy.persistence.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * SupplierProperty JPA Entity.
 * 공급자-숙소 매핑 정보를 매핑하는 순수 데이터 매핑 객체.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Entity
@Table(name = "supplier_property")
public class SupplierPropertyJpaEntity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long supplierId;

    @Column(nullable = false)
    private Long propertyId;

    @Column(nullable = false, length = 100)
    private String supplierPropertyCode;

    private Instant lastSyncedAt;

    @Column(nullable = false, length = 30)
    private String status;

    protected SupplierPropertyJpaEntity() {
        super();
    }

    private SupplierPropertyJpaEntity(Long id, Long supplierId, Long propertyId,
                                       String supplierPropertyCode, Instant lastSyncedAt, String status,
                                       Instant createdAt, Instant updatedAt, Instant deletedAt) {
        super(createdAt, updatedAt, deletedAt);
        this.id = id;
        this.supplierId = supplierId;
        this.propertyId = propertyId;
        this.supplierPropertyCode = supplierPropertyCode;
        this.lastSyncedAt = lastSyncedAt;
        this.status = status;
    }

    public static SupplierPropertyJpaEntity create(Long id, Long supplierId, Long propertyId,
                                                     String supplierPropertyCode, Instant lastSyncedAt, String status,
                                                     Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new SupplierPropertyJpaEntity(id, supplierId, propertyId,
                supplierPropertyCode, lastSyncedAt, status, createdAt, updatedAt, deletedAt);
    }

    public Long getId() { return id; }
    public Long getSupplierId() { return supplierId; }
    public Long getPropertyId() { return propertyId; }
    public String getSupplierPropertyCode() { return supplierPropertyCode; }
    public Instant getLastSyncedAt() { return lastSyncedAt; }
    public String getStatus() { return status; }
}
