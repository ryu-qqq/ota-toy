package com.ryuqq.otatoy.domain.supplier;

/**
 * 이미 정지된 공급자를 다시 정지하려 할 때 발생하는 예외.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public class SupplierAlreadySuspendedException extends SupplierException {

    public SupplierAlreadySuspendedException() {
        super(SupplierErrorCode.SUPPLIER_ALREADY_SUSPENDED);
    }
}
