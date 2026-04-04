package com.ryuqq.otatoy.domain.supplier;

public class SupplierAlreadyTerminatedException extends SupplierException {

    public SupplierAlreadyTerminatedException() {
        super(SupplierErrorCode.SUPPLIER_ALREADY_TERMINATED);
    }
}
