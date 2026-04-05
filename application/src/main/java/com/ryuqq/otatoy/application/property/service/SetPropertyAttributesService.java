package com.ryuqq.otatoy.application.property.service;

import com.ryuqq.otatoy.application.property.dto.command.SetPropertyAttributesCommand;
import com.ryuqq.otatoy.application.property.factory.PropertyAttributeValueFactory;
import com.ryuqq.otatoy.application.property.manager.PropertyAttributeValueCommandManager;
import com.ryuqq.otatoy.application.property.manager.PropertyAttributeValueReadManager;
import com.ryuqq.otatoy.application.property.port.in.SetPropertyAttributesUseCase;
import com.ryuqq.otatoy.application.property.validator.PropertyAttributesValidator;
import com.ryuqq.otatoy.domain.property.PropertyAttributeValueDiff;
import com.ryuqq.otatoy.domain.property.PropertyAttributeValues;

import org.springframework.stereotype.Service;

/**
 * 숙소 속성값 설정 Service.
 * diff 패턴으로 기존/신규를 비교하여 추가/삭제/유지를 도메인에서 판단한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Service
public class SetPropertyAttributesService implements SetPropertyAttributesUseCase {

    private final PropertyAttributesValidator validator;
    private final PropertyAttributeValueFactory propertyAttributeValueFactory;
    private final PropertyAttributeValueReadManager propertyAttributeValueReadManager;
    private final PropertyAttributeValueCommandManager propertyAttributeValueCommandManager;

    public SetPropertyAttributesService(PropertyAttributesValidator validator,
                                         PropertyAttributeValueFactory propertyAttributeValueFactory,
                                         PropertyAttributeValueReadManager propertyAttributeValueReadManager,
                                         PropertyAttributeValueCommandManager propertyAttributeValueCommandManager) {
        this.validator = validator;
        this.propertyAttributeValueFactory = propertyAttributeValueFactory;
        this.propertyAttributeValueReadManager = propertyAttributeValueReadManager;
        this.propertyAttributeValueCommandManager = propertyAttributeValueCommandManager;
    }

    @Override
    public void execute(SetPropertyAttributesCommand command) {
        validator.validate(command);

        PropertyAttributeValues existing = propertyAttributeValueReadManager.getByPropertyId(command.propertyId());
        PropertyAttributeValues newValues = propertyAttributeValueFactory.create(command);
        PropertyAttributeValueDiff diff = existing.update(newValues);

        if (!diff.hasNoChanges()) {
            propertyAttributeValueCommandManager.persistAll(diff.allPersistTargets());
        }
    }
}
