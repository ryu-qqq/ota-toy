package com.ryuqq.otatoy.domain.supplier;

public class SupplierAlreadySuspendedException extends SupplierException {

    public SupplierAlreadySuspendedException() {
        super(SupplierErrorCode.SUPPLIER_ALREADY_SUSPENDED);
    }
}
