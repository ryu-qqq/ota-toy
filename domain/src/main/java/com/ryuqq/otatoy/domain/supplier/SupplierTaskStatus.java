package com.ryuqq.otatoy.domain.supplier;

import java.util.Map;
import java.util.Set;

/**
 * 공급자 작업 상태.
 * PENDING: 대기 중 (생성 직후).
 * PROCESSING: 처리 중 (스케줄러가 소비).
 * COMPLETED: 완료 (외부 API 호출 + 로데이터 저장 성공).
 * FAILED: 실패 (재시도 가능).
 *
 * 상태 전이 규칙:
 * PENDING → PROCESSING (스케줄러 소비)
 * PROCESSING → COMPLETED (성공)
 * PROCESSING → FAILED (실패)
 * FAILED → PENDING (재시도)
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public enum SupplierTaskStatus {

    PENDING("대기"),
    PROCESSING("처리 중"),
    COMPLETED("완료"),
    FAILED("실패");

    private static final Map<SupplierTaskStatus, Set<SupplierTaskStatus>> TRANSITIONS = Map.of(
            PENDING, Set.of(PROCESSING),
            PROCESSING, Set.of(COMPLETED, FAILED, PENDING),
            FAILED, Set.of(PENDING)
    );

    private final String displayName;

    SupplierTaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public boolean canTransitTo(SupplierTaskStatus target) {
        return TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    public SupplierTaskStatus transitTo(SupplierTaskStatus target) {
        if (!canTransitTo(target)) {
            throw new InvalidSupplierTaskStateTransitionException(this, target);
        }
        return target;
    }

    public String displayName() {
        return displayName;
    }
}
