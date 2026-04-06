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
@Table(name = "supplier_task")
public class SupplierTaskJpaEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long supplierId;

    @Column(nullable = false)
    private Long supplierApiConfigId;

    @Column(nullable = false, length = 30)
    private String taskType;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private int retryCount;

    @Column(nullable = false)
    private int maxRetries;

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    private Instant processedAt;

    protected SupplierTaskJpaEntity() {
        super();
    }

    private SupplierTaskJpaEntity(Long id, Long supplierId, Long supplierApiConfigId,
                                   String taskType, String status, String payload,
                                   int retryCount, int maxRetries, String failureReason,
                                   Instant processedAt, Instant createdAt, Instant updatedAt) {
        super(createdAt, updatedAt);
        this.id = id;
        this.supplierId = supplierId;
        this.supplierApiConfigId = supplierApiConfigId;
        this.taskType = taskType;
        this.status = status;
        this.payload = payload;
        this.retryCount = retryCount;
        this.maxRetries = maxRetries;
        this.failureReason = failureReason;
        this.processedAt = processedAt;
    }

    public static SupplierTaskJpaEntity create(Long id, Long supplierId, Long supplierApiConfigId,
                                                String taskType, String status, String payload,
                                                int retryCount, int maxRetries, String failureReason,
                                                Instant processedAt, Instant createdAt, Instant updatedAt) {
        return new SupplierTaskJpaEntity(id, supplierId, supplierApiConfigId,
                taskType, status, payload, retryCount, maxRetries, failureReason,
                processedAt, createdAt, updatedAt);
    }

    public Long getId() { return id; }
    public Long getSupplierId() { return supplierId; }
    public Long getSupplierApiConfigId() { return supplierApiConfigId; }
    public String getTaskType() { return taskType; }
    public String getStatus() { return status; }
    public String getPayload() { return payload; }
    public int getRetryCount() { return retryCount; }
    public int getMaxRetries() { return maxRetries; }
    public String getFailureReason() { return failureReason; }
    public Instant getProcessedAt() { return processedAt; }
}
