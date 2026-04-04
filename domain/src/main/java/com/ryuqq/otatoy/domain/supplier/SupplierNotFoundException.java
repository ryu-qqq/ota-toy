package com.ryuqq.otatoy.domain.supplier;

/**
 * 공급자를 찾을 수 없을 때 발생하는 예외.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public class SupplierNotFoundException extends SupplierException {

    public SupplierNotFoundException() {
        super(SupplierErrorCode.SUPPLIER_NOT_FOUND);
    }
}
