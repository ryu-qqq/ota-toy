package com.ryuqq.otatoy.persistence.supplier.entity;

import com.ryuqq.otatoy.persistence.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * SupplierRawData JPA Entity.
 * 외부 공급자로부터 수집한 원시 데이터를 매핑하는 순수 데이터 매핑 객체.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Entity
@Table(name = "supplier_raw_data")
public class SupplierRawDataJpaEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long supplierId;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String rawPayload;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private Instant fetchedAt;

    private Instant processedAt;

    protected SupplierRawDataJpaEntity() {
        super();
    }

    private SupplierRawDataJpaEntity(Long id, Long supplierId, String rawPayload, String status,
                                      Instant fetchedAt, Instant processedAt,
                                      Instant createdAt, Instant updatedAt) {
        super(createdAt, updatedAt);
        this.id = id;
        this.supplierId = supplierId;
        this.rawPayload = rawPayload;
        this.status = status;
        this.fetchedAt = fetchedAt;
        this.processedAt = processedAt;
    }

    public static SupplierRawDataJpaEntity create(Long id, Long supplierId, String rawPayload, String status,
                                                    Instant fetchedAt, Instant processedAt,
                                                    Instant createdAt, Instant updatedAt) {
        return new SupplierRawDataJpaEntity(id, supplierId, rawPayload, status,
                fetchedAt, processedAt, createdAt, updatedAt);
    }

    public Long getId() { return id; }
    public Long getSupplierId() { return supplierId; }
    public String getRawPayload() { return rawPayload; }
    public String getStatus() { return status; }
    public Instant getFetchedAt() { return fetchedAt; }
    public Instant getProcessedAt() { return processedAt; }
}
