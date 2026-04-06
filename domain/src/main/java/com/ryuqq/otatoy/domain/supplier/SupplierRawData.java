package com.ryuqq.otatoy.domain.supplier;

import java.time.Instant;
import java.util.Objects;

/**
 * 외부 공급자로부터 수집한 원시 데이터를 나타내는 엔티티.
 * 수집(FETCHED) → 가공(PROCESSING) → 동기화(SYNCED) 또는 실패(FAILED) 상태로 전이한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public class SupplierRawData {

    private final SupplierRawDataId id;
    private final SupplierId supplierId;
    private final String rawPayload;
    private SupplierRawDataStatus status;
    private final Instant fetchedAt;
    private Instant processedAt;
    private final Instant createdAt;
    private Instant updatedAt;

    private SupplierRawData(SupplierRawDataId id, SupplierId supplierId, String rawPayload,
                            SupplierRawDataStatus status, Instant fetchedAt, Instant processedAt,
                            Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.supplierId = supplierId;
        this.rawPayload = rawPayload;
        this.status = status;
        this.fetchedAt = fetchedAt;
        this.processedAt = processedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SupplierRawData forNew(SupplierId supplierId, String rawPayload, Instant now) {
        validate(rawPayload);
        return new SupplierRawData(
                SupplierRawDataId.forNew(), supplierId, rawPayload,
                SupplierRawDataStatus.FETCHED, now, null, now, now
        );
    }

    public static SupplierRawData reconstitute(SupplierRawDataId id, SupplierId supplierId, String rawPayload,
                                                SupplierRawDataStatus status, Instant fetchedAt,
                                                Instant processedAt, Instant createdAt, Instant updatedAt) {
        return new SupplierRawData(id, supplierId, rawPayload, status, fetchedAt, processedAt, createdAt, updatedAt);
    }

    private static void validate(String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            throw new IllegalArgumentException("원시 데이터 페이로드는 필수입니다");
        }
    }

    public void markProcessing() {
        if (this.status != SupplierRawDataStatus.FETCHED) {
            throw new IllegalStateException("FETCHED 상태에서만 가공을 시작할 수 있습니다. 현재: " + this.status);
        }
        this.status = SupplierRawDataStatus.PROCESSING;
    }

    public void markSynced(Instant now) {
        if (this.status != SupplierRawDataStatus.PROCESSING) {
            throw new IllegalStateException("PROCESSING 상태에서만 동기화 완료할 수 있습니다. 현재: " + this.status);
        }
        this.status = SupplierRawDataStatus.SYNCED;
        this.processedAt = now;
        this.updatedAt = now;
    }

    public void markFailed(Instant now) {
        this.status = SupplierRawDataStatus.FAILED;
        this.processedAt = now;
        this.updatedAt = now;
    }

    public SupplierRawDataId id() { return id; }
    public SupplierId supplierId() { return supplierId; }
    public String rawPayload() { return rawPayload; }
    public SupplierRawDataStatus status() { return status; }
    public Instant fetchedAt() { return fetchedAt; }
    public Instant processedAt() { return processedAt; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SupplierRawData s)) return false;
        return id != null && id.value() != null && id.equals(s.id);
    }

    @Override
    public int hashCode() {
        return id != null && id.value() != null ? Objects.hashCode(id) : System.identityHashCode(this);
    }
}
