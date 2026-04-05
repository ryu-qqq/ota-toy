package com.ryuqq.otatoy.domain.property;

import com.ryuqq.otatoy.domain.accommodation.AccommodationErrorCode;
import com.ryuqq.otatoy.domain.common.DomainException;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeAttributeId;

import java.util.Set;

/**
 * 필수 속성이 누락되었을 때 발생하는 예외.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public class RequiredPropertyAttributeMissingException extends DomainException {

    private final Set<PropertyTypeAttributeId> missingAttributeIds;

    public RequiredPropertyAttributeMissingException(Set<PropertyTypeAttributeId> missingAttributeIds) {
        super(AccommodationErrorCode.REQUIRED_ATTRIBUTE_MISSING);
        this.missingAttributeIds = missingAttributeIds;
    }

    public Set<PropertyTypeAttributeId> missingAttributeIds() {
        return missingAttributeIds;
    }
}
