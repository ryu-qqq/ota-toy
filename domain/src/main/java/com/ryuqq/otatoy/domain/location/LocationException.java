package com.ryuqq.otatoy.domain.location;

import com.ryuqq.otatoy.domain.common.DomainException;

public class LocationException extends DomainException {

    protected LocationException(LocationErrorCode errorCode) {
        super(errorCode);
    }
}
