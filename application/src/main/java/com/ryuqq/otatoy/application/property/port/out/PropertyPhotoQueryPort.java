package com.ryuqq.otatoy.application.property.port.out;

import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.property.PropertyPhoto;

import java.util.List;

public interface PropertyPhotoQueryPort {
    List<PropertyPhoto> findByPropertyId(PropertyId propertyId);
}
