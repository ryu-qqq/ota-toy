package com.ryuqq.otatoy.application.property.validator;

import com.ryuqq.otatoy.application.property.manager.PropertyReadManager;
import com.ryuqq.otatoy.domain.property.PropertyId;

import org.springframework.stereotype.Component;

/**
 * 편의시설 설정 검증 전용 Validator.
 * PropertyReadManager.verifyExists를 경유하여 Property 존재 여부를 확인한다 (APP-VAL-002).
 * Validator에는 @Transactional을 선언하지 않는다 -- ReadManager가 트랜잭션을 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class PropertyAmenitiesValidator {

    private final PropertyReadManager propertyReadManager;

    public PropertyAmenitiesValidator(PropertyReadManager propertyReadManager) {
        this.propertyReadManager = propertyReadManager;
    }

    /**
     * Property 존재 여부를 검증한다.
     * 존재하지 않으면 PropertyNotFoundException 발생.
     */
    public void validate(PropertyId propertyId) {
        propertyReadManager.verifyExists(propertyId);
    }
}
