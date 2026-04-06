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
 * SupplierSyncLog JPA Entity.
 * 공급자 동기화 로그를 매핑하는 순수 데이터 매핑 객체.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Entity
@Table(name = "supplier_sync_log")
public class SupplierSyncLogJpaEntity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long supplierId;

    @Column(nullable = false, length = 30)
    private String syncType;

    @Column(nullable = false)
    private Instant syncedAt;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(nullable = false)
    private int totalCount;

    @Column(nullable = false)
    private int createdCount;

    @Column(nullable = false)
    private int updatedCount;

    @Column(nullable = false)
    private int deletedCount;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    protected SupplierSyncLogJpaEntity() {
        super();
    }

    private SupplierSyncLogJpaEntity(Long id, Long supplierId, String syncType, Instant syncedAt,
                                      String status, int totalCount, int createdCount,
                                      int updatedCount, int deletedCount, String errorMessage,
                                      Instant createdAt, Instant updatedAt, Instant deletedAt) {
        super(createdAt, updatedAt, deletedAt);
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

    public static SupplierSyncLogJpaEntity create(Long id, Long supplierId, String syncType, Instant syncedAt,
                                                     String status, int totalCount, int createdCount,
                                                     int updatedCount, int deletedCount, String errorMessage,
                                                     Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new SupplierSyncLogJpaEntity(id, supplierId, syncType, syncedAt,
                status, totalCount, createdCount, updatedCount, deletedCount, errorMessage,
                createdAt, updatedAt, deletedAt);
    }

    public Long getId() { return id; }
    public Long getSupplierId() { return supplierId; }
    public String getSyncType() { return syncType; }
    public Instant getSyncedAt() { return syncedAt; }
    public String getStatus() { return status; }
    public int getTotalCount() { return totalCount; }
    public int getCreatedCount() { return createdCount; }
    public int getUpdatedCount() { return updatedCount; }
    public int getDeletedCount() { return deletedCount; }
    public String getErrorMessage() { return errorMessage; }
}
