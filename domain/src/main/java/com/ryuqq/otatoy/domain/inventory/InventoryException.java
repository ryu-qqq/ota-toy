package com.ryuqq.otatoy.domain.inventory;

import com.ryuqq.otatoy.domain.common.DomainException;

public class InventoryException extends DomainException {

    protected InventoryException(InventoryErrorCode errorCode) {
        super(errorCode);
    }
}
