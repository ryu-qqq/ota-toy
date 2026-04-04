package com.ryuqq.otatoy.domain.accommodation;

import com.ryuqq.otatoy.domain.common.DomainException;

public class PropertyNotFoundException extends DomainException {

    public PropertyNotFoundException() {
        super(AccommodationErrorCode.PROPERTY_NOT_FOUND);
    }
}
