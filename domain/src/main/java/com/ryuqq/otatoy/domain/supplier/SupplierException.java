package com.ryuqq.otatoy.domain.supplier;

import com.ryuqq.otatoy.domain.common.DomainException;

public class SupplierException extends DomainException {

    protected SupplierException(SupplierErrorCode errorCode) {
        super(errorCode);
    }
}
