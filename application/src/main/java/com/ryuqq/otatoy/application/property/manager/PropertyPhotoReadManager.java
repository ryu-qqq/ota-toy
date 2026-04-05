package com.ryuqq.otatoy.application.property.manager;

import com.ryuqq.otatoy.application.property.port.out.PropertyPhotoQueryPort;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.property.PropertyPhotos;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PropertyPhotoReadManager {

    private final PropertyPhotoQueryPort propertyPhotoQueryPort;

    public PropertyPhotoReadManager(PropertyPhotoQueryPort propertyPhotoQueryPort) {
        this.propertyPhotoQueryPort = propertyPhotoQueryPort;
    }

    @Transactional(readOnly = true)
    public PropertyPhotos getByPropertyId(PropertyId propertyId) {
        return PropertyPhotos.reconstitute(propertyPhotoQueryPort.findByPropertyId(propertyId));
    }
}
