package com.ryuqq.otatoy.application.property.service;

import com.ryuqq.otatoy.application.property.dto.command.SetPropertyPhotosCommand;
import com.ryuqq.otatoy.application.property.factory.PropertyPhotoFactory;
import com.ryuqq.otatoy.application.property.manager.PropertyPhotoCommandManager;
import com.ryuqq.otatoy.application.property.manager.PropertyPhotoReadManager;
import com.ryuqq.otatoy.application.property.port.in.SetPropertyPhotosUseCase;
import com.ryuqq.otatoy.application.property.validator.PropertyPhotosValidator;
import com.ryuqq.otatoy.domain.property.PropertyPhotoDiff;
import com.ryuqq.otatoy.domain.property.PropertyPhotos;

import org.springframework.stereotype.Service;

/**
 * 숙소 사진 설정 Service.
 * diff 패턴으로 기존/신규를 비교하여 추가/삭제/유지를 도메인에서 판단한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Service
public class SetPropertyPhotosService implements SetPropertyPhotosUseCase {

    private final PropertyPhotosValidator validator;
    private final PropertyPhotoFactory propertyPhotoFactory;
    private final PropertyPhotoReadManager propertyPhotoReadManager;
    private final PropertyPhotoCommandManager propertyPhotoCommandManager;

    public SetPropertyPhotosService(PropertyPhotosValidator validator,
                                     PropertyPhotoFactory propertyPhotoFactory,
                                     PropertyPhotoReadManager propertyPhotoReadManager,
                                     PropertyPhotoCommandManager propertyPhotoCommandManager) {
        this.validator = validator;
        this.propertyPhotoFactory = propertyPhotoFactory;
        this.propertyPhotoReadManager = propertyPhotoReadManager;
        this.propertyPhotoCommandManager = propertyPhotoCommandManager;
    }

    @Override
    public void execute(SetPropertyPhotosCommand command) {
        validator.validate(command.propertyId());

        PropertyPhotos existing = propertyPhotoReadManager.getByPropertyId(command.propertyId());
        PropertyPhotos newPhotos = propertyPhotoFactory.createPhotos(command);
        PropertyPhotoDiff diff = existing.update(newPhotos);

        if (!diff.hasNoChanges()) {
            propertyPhotoCommandManager.persistAll(diff.allPersistTargets());
        }
    }
}
