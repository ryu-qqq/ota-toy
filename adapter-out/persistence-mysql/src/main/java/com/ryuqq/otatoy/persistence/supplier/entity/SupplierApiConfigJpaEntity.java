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
 * SupplierApiConfig JPA Entity.
 * 공급자 API 연동 설정을 매핑하는 순수 데이터 매핑 객체.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Entity
@Table(name = "supplier_api_config")
public class SupplierApiConfigJpaEntity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long supplierId;

    @Column(nullable = false, length = 30)
    private String apiType;

    @Column(nullable = false, length = 500)
    private String apiBaseUrl;

    @Column(nullable = false, length = 500)
    private String apiKey;

    @Column(nullable = false, length = 30)
    private String authType;

    @Column(nullable = false)
    private int syncIntervalMinutes;

    @Column(nullable = false, length = 30)
    private String status;

    protected SupplierApiConfigJpaEntity() {
        super();
    }

    private SupplierApiConfigJpaEntity(Long id, Long supplierId, String apiType,
                                        String apiBaseUrl, String apiKey, String authType,
                                        int syncIntervalMinutes, String status,
                                        Instant createdAt, Instant updatedAt, Instant deletedAt) {
        super(createdAt, updatedAt, deletedAt);
        this.id = id;
        this.supplierId = supplierId;
        this.apiType = apiType;
        this.apiBaseUrl = apiBaseUrl;
        this.apiKey = apiKey;
        this.authType = authType;
        this.syncIntervalMinutes = syncIntervalMinutes;
        this.status = status;
    }

    public static SupplierApiConfigJpaEntity create(Long id, Long supplierId, String apiType,
                                                      String apiBaseUrl, String apiKey, String authType,
                                                      int syncIntervalMinutes, String status,
                                                      Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new SupplierApiConfigJpaEntity(id, supplierId, apiType, apiBaseUrl, apiKey, authType,
                syncIntervalMinutes, status, createdAt, updatedAt, deletedAt);
    }

    public Long getId() { return id; }
    public Long getSupplierId() { return supplierId; }
    public String getApiType() { return apiType; }
    public String getApiBaseUrl() { return apiBaseUrl; }
    public String getApiKey() { return apiKey; }
    public String getAuthType() { return authType; }
    public int getSyncIntervalMinutes() { return syncIntervalMinutes; }
    public String getStatus() { return status; }
}
