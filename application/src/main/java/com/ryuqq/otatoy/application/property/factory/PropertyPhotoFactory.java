package com.ryuqq.otatoy.application.property.factory;

import com.ryuqq.otatoy.application.common.factory.TimeProvider;
import com.ryuqq.otatoy.application.property.dto.command.SetPropertyPhotosCommand;
import com.ryuqq.otatoy.domain.property.PropertyPhoto;
import com.ryuqq.otatoy.domain.property.PropertyPhotos;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class PropertyPhotoFactory {

    private final TimeProvider timeProvider;

    public PropertyPhotoFactory(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public PropertyPhotos createPhotos(SetPropertyPhotosCommand command) {
        Instant now = timeProvider.now();

        List<PropertyPhoto> items = command.photos().stream()
            .map(item -> PropertyPhoto.forNew(
                command.propertyId(),
                item.photoType(),
                item.originUrl(),
                item.cdnUrl(),
                item.sortOrder(),
                now
            ))
            .toList();

        return PropertyPhotos.forNew(items);
    }
}
