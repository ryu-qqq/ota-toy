package com.ryuqq.otatoy.domain.supplier;

/**
 * 허용되지 않는 공급자 상태 전이를 시도할 때 발생하는 예외.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public class InvalidSupplierStateTransitionException extends SupplierException {

    public InvalidSupplierStateTransitionException() {
        super(SupplierErrorCode.INVALID_SUPPLIER_STATE_TRANSITION);
    }
}
