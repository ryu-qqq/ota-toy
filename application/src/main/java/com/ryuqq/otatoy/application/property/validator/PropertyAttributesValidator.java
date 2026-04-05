package com.ryuqq.otatoy.application.property.validator;

import com.ryuqq.otatoy.application.property.dto.command.SetPropertyAttributesCommand;
import com.ryuqq.otatoy.application.property.manager.PropertyReadManager;
import com.ryuqq.otatoy.application.propertytype.manager.PropertyTypeReadManager;
import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.RequiredPropertyAttributeMissingException;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeAttributeId;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 숙소 속성값 설정 검증 전용 Validator.
 * ReadManager를 주입받아 숙소 존재 여부 및 필수 속성 누락 여부를 확인한다 (APP-VAL-002).
 * Validator에는 @Transactional을 선언하지 않는다 -- ReadManager가 트랜잭션을 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class PropertyAttributesValidator {

    private final PropertyReadManager propertyReadManager;
    private final PropertyTypeReadManager propertyTypeReadManager;

    public PropertyAttributesValidator(PropertyReadManager propertyReadManager,
                                        PropertyTypeReadManager propertyTypeReadManager) {
        this.propertyReadManager = propertyReadManager;
        this.propertyTypeReadManager = propertyTypeReadManager;
    }

    /**
     * 속성값 설정 전 숙소 존재 여부와 필수 속성 누락 여부를 검증한다.
     *
     * @throws com.ryuqq.otatoy.domain.property.PropertyNotFoundException 숙소가 존재하지 않을 때
     * @throws RequiredPropertyAttributeMissingException 필수 속성이 누락되었을 때
     */
    public void validate(SetPropertyAttributesCommand command) {
        // 1. 숙소 존재 확인 + propertyTypeId 획득 (AC-2)
        Property property = propertyReadManager.getById(command.propertyId());

        // 2. 해당 숙소 유형의 필수 속성 ID 조회 (AC-3)
        Set<PropertyTypeAttributeId> requiredAttributeIds =
            propertyTypeReadManager.getRequiredAttributeIds(property.propertyTypeId());

        if (requiredAttributeIds.isEmpty()) {
            return;
        }

        // 3. 요청에 포함된 속성 ID 집합
        Set<PropertyTypeAttributeId> providedAttributeIds = command.attributes().stream()
            .map(SetPropertyAttributesCommand.AttributeItem::propertyTypeAttributeId)
            .collect(Collectors.toSet());

        // 4. 필수 속성 누락 검사
        Set<PropertyTypeAttributeId> missingAttributes = requiredAttributeIds.stream()
            .filter(id -> !providedAttributeIds.contains(id))
            .collect(Collectors.toSet());

        if (!missingAttributes.isEmpty()) {
            throw new RequiredPropertyAttributeMissingException(missingAttributes);
        }
    }
}
