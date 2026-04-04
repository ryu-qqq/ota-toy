package com.ryuqq.otatoy.domain.inventory;

import com.ryuqq.otatoy.domain.common.DomainException;

/**
 * 재고 도메인의 기본 예외.
 */
public class InventoryException extends DomainException {

    protected InventoryException(InventoryErrorCode errorCode) {
        super(errorCode);
    }
}
