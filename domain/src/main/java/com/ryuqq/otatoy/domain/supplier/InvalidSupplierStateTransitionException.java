package com.ryuqq.otatoy.domain.supplier;

public class InvalidSupplierStateTransitionException extends SupplierException {

    public InvalidSupplierStateTransitionException() {
        super(SupplierErrorCode.INVALID_SUPPLIER_STATE_TRANSITION);
    }
}
