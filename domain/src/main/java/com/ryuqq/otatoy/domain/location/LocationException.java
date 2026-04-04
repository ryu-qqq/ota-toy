package com.ryuqq.otatoy.domain.location;

import com.ryuqq.otatoy.domain.common.DomainException;

/**
 * 위치 도메인의 기본 예외.
 */
public class LocationException extends DomainException {

    protected LocationException(LocationErrorCode errorCode) {
        super(errorCode);
    }
}
