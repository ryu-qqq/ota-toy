package com.ryuqq.otatoy.domain.supplier;

import com.ryuqq.otatoy.domain.common.DomainException;

/**
 * 공급자 도메인의 기본 예외.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public class SupplierException extends DomainException {

    protected SupplierException(SupplierErrorCode errorCode) {
        super(errorCode);
    }
}
