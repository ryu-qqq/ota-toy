package com.ryuqq.otatoy.domain.supplier;

/**
 * 공급자 작업의 재시도 횟수가 소진되었을 때 발생하는 예외.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public class SupplierTaskRetryExhaustedException extends SupplierException {

    public SupplierTaskRetryExhaustedException() {
        super(SupplierErrorCode.SUPPLIER_TASK_RETRY_EXHAUSTED);
    }
}
