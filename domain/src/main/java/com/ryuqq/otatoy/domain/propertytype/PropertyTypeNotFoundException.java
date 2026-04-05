package com.ryuqq.otatoy.domain.propertytype;

import com.ryuqq.otatoy.domain.common.DomainException;

/**
 * 숙소 유형을 찾을 수 없을 때 발생하는 예외.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public class PropertyTypeNotFoundException extends DomainException {

    public PropertyTypeNotFoundException() {
        super(PropertyTypeErrorCode.PROPERTY_TYPE_NOT_FOUND);
    }
}
