package com.ryuqq.otatoy.application.property.validator;

import com.ryuqq.otatoy.application.property.manager.PropertyReadManager;
import com.ryuqq.otatoy.domain.property.PropertyId;

import org.springframework.stereotype.Component;

@Component
public class PropertyPhotosValidator {

    private final PropertyReadManager propertyReadManager;

    public PropertyPhotosValidator(PropertyReadManager propertyReadManager) {
        this.propertyReadManager = propertyReadManager;
    }

    public void validate(PropertyId propertyId) {
        propertyReadManager.verifyExists(propertyId);
    }
}
