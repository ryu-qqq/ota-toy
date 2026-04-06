package com.ryuqq.otatoy.domain.supplier;

import java.time.Instant;
import java.util.Objects;

/**
 * 공급자 작업(Outbox 역할)을 나타내는 Aggregate Root.
 * 스케줄러가 직접 소비하는 구조로, SQS 없이 Outbox 패턴을 구현한다.
 *
 * 생명주기:
 * 1. forNew()로 PENDING 상태 생성
 * 2. markProcessing()으로 스케줄러가 소비
 * 3. markCompleted() 또는 markFailed()로 완료/실패 처리
 * 4. 실패 시 resetToPending()으로 재시도 (maxRetries 이내)
 *
 * @author ryu-qqq
 * @since 2026-04-06
 * @see SupplierTaskStatus 상태 전이 규칙
 * @see SupplierTaskFailureReason 실패 사유 VO
 */
public class SupplierTask {

    private final SupplierTaskId id;
    private final SupplierId supplierId;
    private final Long supplierApiConfigId;
    private final SupplierTaskType taskType;
    private SupplierTaskStatus status;
    private final String payload;
    private int retryCount;
    private final int maxRetries;
    private String failureReason;
    private final Instant createdAt;
    private Instant processedAt;

    private SupplierTask(SupplierTaskId id, SupplierId supplierId, Long supplierApiConfigId,
                         SupplierTaskType taskType, SupplierTaskStatus status, String payload,
                         int retryCount, int maxRetries, String failureReason,
                         Instant createdAt, Instant processedAt) {
        this.id = id;
        this.supplierId = supplierId;
        this.supplierApiConfigId = supplierApiConfigId;
        this.taskType = taskType;
        this.status = status;
        this.payload = payload;
        this.retryCount = retryCount;
        this.maxRetries = maxRetries;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    /**
     * 신규 공급자 작업을 생성한다. PENDING 상태로 시작한다.
     */
    public static SupplierTask forNew(SupplierId supplierId, Long supplierApiConfigId,
                                       SupplierTaskType taskType, String payload,
                                       int maxRetries, Instant now) {
        if (supplierId == null) {
            throw new IllegalArgumentException("공급자 ID는 필수입니다");
        }
        if (supplierApiConfigId == null) {
            throw new IllegalArgumentException("공급자 API 설정 ID는 필수입니다");
        }
        if (taskType == null) {
            throw new IllegalArgumentException("작업 유형은 필수입니다");
        }
        if (maxRetries < 0) {
            throw new IllegalArgumentException("최대 재시도 횟수는 0 이상이어야 합니다");
        }
        if (now == null) {
            throw new IllegalArgumentException("생성 시각은 필수입니다");
        }
        return new SupplierTask(SupplierTaskId.forNew(), supplierId, supplierApiConfigId,
                taskType, SupplierTaskStatus.PENDING, payload, 0, maxRetries, null, now, null);
    }

    /**
     * DB에서 복원한다. 검증 없이 모든 필드를 그대로 복원한다.
     */
    public static SupplierTask reconstitute(SupplierTaskId id, SupplierId supplierId, Long supplierApiConfigId,
                                             SupplierTaskType taskType, SupplierTaskStatus status, String payload,
                                             int retryCount, int maxRetries, String failureReason,
                                             Instant createdAt, Instant processedAt) {
        return new SupplierTask(id, supplierId, supplierApiConfigId, taskType, status, payload,
                retryCount, maxRetries, failureReason, createdAt, processedAt);
    }

    /**
     * PENDING → PROCESSING 전이. 스케줄러가 작업을 소비할 때 호출한다.
     */
    public void markProcessing() {
        this.status = status.transitTo(SupplierTaskStatus.PROCESSING);
    }

    /**
     * PROCESSING → COMPLETED 전이. 외부 API 호출 성공 시 호출한다.
     */
    public void markCompleted(Instant now) {
        this.status = status.transitTo(SupplierTaskStatus.COMPLETED);
        this.processedAt = now;
    }

    /**
     * PROCESSING → FAILED 전이. 외부 API 호출 실패 시 호출한다.
     * retryCount를 증가시키고 실패 사유를 기록한다.
     */
    public void markFailed(String failureReason, Instant now) {
        this.status = status.transitTo(SupplierTaskStatus.FAILED);
        this.retryCount++;
        this.failureReason = failureReason;
        this.processedAt = now;
    }

    /**
     * PROCESSING → PENDING 전이. Circuit Breaker OPEN 등 외부 서비스 불가 시 호출한다.
     * retryCount를 증가시키지 않는다 — 외부 서비스 장애이므로 우리 재시도 횟수로 세지 않는다.
     */
    public void deferRetry() {
        this.status = status.transitTo(SupplierTaskStatus.PENDING);
        this.processedAt = null;
    }

    /**
     * FAILED → PENDING 전이. 재시도 시 호출한다.
     * 재시도 횟수가 소진되었으면 예외를 발생시킨다.
     */
    public void resetToPending() {
        if (this.status != SupplierTaskStatus.FAILED) {
            throw new InvalidSupplierTaskStateTransitionException(this.status, SupplierTaskStatus.PENDING);
        }
        if (!canRetry()) {
            throw new SupplierTaskRetryExhaustedException();
        }
        this.status = status.transitTo(SupplierTaskStatus.PENDING);
        this.processedAt = null;
    }

    /**
     * 재시도 가능 여부를 반환한다.
     */
    public boolean canRetry() {
        return retryCount < maxRetries;
    }

    /**
     * 종료 상태인지 판단한다.
     * COMPLETED이거나 FAILED이면서 재시도 불가인 경우 종료 상태다.
     */
    public boolean isTaskType(SupplierTaskType type) {
        return this.taskType == type;
    }

    public boolean isTerminal() {
        return status == SupplierTaskStatus.COMPLETED
                || (status == SupplierTaskStatus.FAILED && !canRetry());
    }

    public SupplierTaskId id() { return id; }
    public SupplierId supplierId() { return supplierId; }
    public Long supplierApiConfigId() { return supplierApiConfigId; }
    public SupplierTaskType taskType() { return taskType; }
    public SupplierTaskStatus status() { return status; }
    public String payload() { return payload; }
    public int retryCount() { return retryCount; }
    public int maxRetries() { return maxRetries; }
    public String failureReason() { return failureReason; }
    public Instant createdAt() { return createdAt; }
    public Instant processedAt() { return processedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SupplierTask t)) return false;
        return id != null && id.value() != null && id.equals(t.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hashCode(id) : System.identityHashCode(this);
    }
}
