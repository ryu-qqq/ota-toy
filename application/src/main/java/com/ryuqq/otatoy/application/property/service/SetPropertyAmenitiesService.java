package com.ryuqq.otatoy.application.property.service;

import com.ryuqq.otatoy.application.property.dto.command.SetPropertyAmenitiesCommand;
import com.ryuqq.otatoy.application.property.factory.PropertyAmenityFactory;
import com.ryuqq.otatoy.application.property.manager.PropertyAmenityCommandManager;
import com.ryuqq.otatoy.application.property.manager.PropertyAmenityReadManager;
import com.ryuqq.otatoy.application.property.port.in.SetPropertyAmenitiesUseCase;
import com.ryuqq.otatoy.application.property.validator.PropertyAmenitiesValidator;
import com.ryuqq.otatoy.domain.property.PropertyAmenities;
import com.ryuqq.otatoy.domain.property.PropertyAmenityDiff;

import org.springframework.stereotype.Service;

/**
 * 숙소 편의시설 설정 Service.
 * diff 패턴으로 기존/신규를 비교하여 추가/삭제/유지를 도메인에서 판단한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Service
public class SetPropertyAmenitiesService implements SetPropertyAmenitiesUseCase {

    private final PropertyAmenitiesValidator validator;
    private final PropertyAmenityFactory propertyAmenityFactory;
    private final PropertyAmenityReadManager propertyAmenityReadManager;
    private final PropertyAmenityCommandManager propertyAmenityCommandManager;

    public SetPropertyAmenitiesService(PropertyAmenitiesValidator validator,
                                        PropertyAmenityFactory propertyAmenityFactory,
                                        PropertyAmenityReadManager propertyAmenityReadManager,
                                        PropertyAmenityCommandManager propertyAmenityCommandManager) {
        this.validator = validator;
        this.propertyAmenityFactory = propertyAmenityFactory;
        this.propertyAmenityReadManager = propertyAmenityReadManager;
        this.propertyAmenityCommandManager = propertyAmenityCommandManager;
    }

    @Override
    public void execute(SetPropertyAmenitiesCommand command) {
        validator.validate(command.propertyId());

        PropertyAmenities existing = propertyAmenityReadManager.getByPropertyId(command.propertyId());
        PropertyAmenities newAmenities = propertyAmenityFactory.createAmenities(command);
        PropertyAmenityDiff diff = existing.update(newAmenities);

        if (!diff.hasNoChanges()) {
            propertyAmenityCommandManager.persistAll(diff.allPersistTargets());
        }
    }
}
