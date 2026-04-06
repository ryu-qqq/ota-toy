package com.ryuqq.otatoy.domain.supplier;

/**
 * 허용되지 않는 공급자 작업 상태 전이를 시도할 때 발생하는 예외.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public class InvalidSupplierTaskStateTransitionException extends SupplierException {

    public InvalidSupplierTaskStateTransitionException(SupplierTaskStatus from, SupplierTaskStatus to) {
        super(SupplierErrorCode.INVALID_SUPPLIER_TASK_STATE_TRANSITION);
    }
}
