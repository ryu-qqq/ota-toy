package com.ryuqq.otatoy.domain.supplier;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * 공급자 API 연동 설정.
 * API 유형, 인증 정보, 동기화 주기를 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public class SupplierApiConfig {

    private final Long id;
    private final SupplierId supplierId;
    private final SupplierApiType apiType;
    private final String baseUrl;
    private final String apiKey;
    private final String authType;
    private final int syncIntervalMinutes;
    private final Instant createdAt;
    private Instant updatedAt;

    private SupplierApiConfig(Long id, SupplierId supplierId, SupplierApiType apiType,
                              String baseUrl, String apiKey, String authType,
                              int syncIntervalMinutes,
                              Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.supplierId = supplierId;
        this.apiType = apiType;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.authType = authType;
        this.syncIntervalMinutes = syncIntervalMinutes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SupplierApiConfig forNew(SupplierId supplierId, SupplierApiType apiType,
                                            String baseUrl, String apiKey, String authType,
                                            int syncIntervalMinutes, Instant now) {
        validateBaseUrl(baseUrl);
        validateInterval(syncIntervalMinutes);
        return new SupplierApiConfig(null, supplierId, apiType, baseUrl, apiKey, authType,
                syncIntervalMinutes, now, now);
    }

    public static SupplierApiConfig reconstitute(Long id, SupplierId supplierId, SupplierApiType apiType,
                                                  String baseUrl, String apiKey, String authType,
                                                  int syncIntervalMinutes,
                                                  Instant createdAt, Instant updatedAt) {
        return new SupplierApiConfig(id, supplierId, apiType, baseUrl, apiKey, authType,
                syncIntervalMinutes, createdAt, updatedAt);
    }

    private static void validateBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("API Base URL은 필수입니다");
        }
    }

    private static void validateInterval(int syncIntervalMinutes) {
        if (syncIntervalMinutes < 1) {
            throw new IllegalArgumentException("동기화 주기는 1분 이상이어야 합니다");
        }
    }

    /**
     * 마지막 수집 시각 기준으로 수집이 필요한지 판별한다.
     * lastFetchedAt이 null이면 한 번도 수집하지 않은 것이므로 즉시 수집 대상.
     */
    public boolean isDueForFetch(Instant lastFetchedAt, Instant now) {
        if (lastFetchedAt == null) {
            return true;
        }
        Duration elapsed = Duration.between(lastFetchedAt, now);
        return elapsed.toMinutes() >= syncIntervalMinutes;
    }

    public Long id() { return id; }
    public SupplierId supplierId() { return supplierId; }
    public SupplierApiType apiType() { return apiType; }
    public String baseUrl() { return baseUrl; }
    public String apiKey() { return apiKey; }
    public String authType() { return authType; }
    public int syncIntervalMinutes() { return syncIntervalMinutes; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SupplierApiConfig s)) return false;
        return id != null && id.equals(s.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hashCode(id) : System.identityHashCode(this);
    }
}
