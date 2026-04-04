package com.ryuqq.otatoy.domain.property;

import com.ryuqq.otatoy.domain.accommodation.AccommodationErrorCode;

import com.ryuqq.otatoy.domain.common.DomainException;

/**
 * 숙소를 찾을 수 없을 때 발생하는 예외.
 */
public class PropertyNotFoundException extends DomainException {

    public PropertyNotFoundException() {
        super(AccommodationErrorCode.PROPERTY_NOT_FOUND);
    }
}
