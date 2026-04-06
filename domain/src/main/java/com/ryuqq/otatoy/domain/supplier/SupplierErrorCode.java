package com.ryuqq.otatoy.domain.supplier;

import com.ryuqq.otatoy.domain.common.ErrorCategory;
import com.ryuqq.otatoy.domain.common.ErrorCode;

public enum SupplierErrorCode implements ErrorCode {

    SUPPLIER_NOT_FOUND("SUP-001", "공급자를 찾을 수 없습니다", ErrorCategory.NOT_FOUND),
    SUPPLIER_ALREADY_SUSPENDED("SUP-002", "이미 정지된 공급자입니다", ErrorCategory.CONFLICT),
    SUPPLIER_SYNC_FAILED("SUP-003", "공급자 동기화에 실패했습니다", ErrorCategory.VALIDATION),
    SUPPLIER_ALREADY_TERMINATED("SUP-004", "이미 해지된 공급자입니다", ErrorCategory.CONFLICT),
    SUPPLIER_ALREADY_ACTIVE("SUP-005", "이미 활성 상태인 공급자입니다", ErrorCategory.CONFLICT),
    INVALID_SUPPLIER_STATE_TRANSITION("SUP-006", "허용되지 않는 공급자 상태 전이입니다", ErrorCategory.VALIDATION),
    INVALID_SUPPLIER_TASK_STATE_TRANSITION("SUP-007", "허용되지 않는 공급자 작업 상태 전이입니다", ErrorCategory.VALIDATION),
    SUPPLIER_TASK_RETRY_EXHAUSTED("SUP-008", "공급자 작업의 재시도 횟수가 소진되었습니다", ErrorCategory.CONFLICT);

    private final String code;
    private final String message;
    private final ErrorCategory category;

    SupplierErrorCode(String code, String message, ErrorCategory category) {
        this.code = code;
        this.message = message;
        this.category = category;
    }

    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
    @Override public ErrorCategory getCategory() { return category; }
}
