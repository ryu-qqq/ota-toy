package com.ryuqq.otatoy.domain.supplier;

public class SupplierNotFoundException extends SupplierException {

    public SupplierNotFoundException() {
        super(SupplierErrorCode.SUPPLIER_NOT_FOUND);
    }
}
