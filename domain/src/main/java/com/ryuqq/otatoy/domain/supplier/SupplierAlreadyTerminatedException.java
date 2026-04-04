package com.ryuqq.otatoy.domain.supplier;

/**
 * 이미 해지된 공급자를 변경하려 할 때 발생하는 예외.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public class SupplierAlreadyTerminatedException extends SupplierException {

    public SupplierAlreadyTerminatedException() {
        super(SupplierErrorCode.SUPPLIER_ALREADY_TERMINATED);
    }
}
