package com.ryuqq.otatoy.application.property.manager;

import com.ryuqq.otatoy.application.property.dto.PropertyDetailBundle;
import com.ryuqq.otatoy.application.property.port.out.PropertyAmenityQueryPort;
import com.ryuqq.otatoy.application.property.port.out.PropertyAttributeValueQueryPort;
import com.ryuqq.otatoy.application.property.port.out.PropertyPhotoQueryPort;
import com.ryuqq.otatoy.application.property.port.out.PropertyQueryPort;
import com.ryuqq.otatoy.application.roomtype.port.out.RoomTypeQueryPort;
import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertyAmenities;
import com.ryuqq.otatoy.domain.property.PropertyAttributeValues;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.property.PropertyNotFoundException;
import com.ryuqq.otatoy.domain.property.PropertyPhotos;
import com.ryuqq.otatoy.domain.roomtype.RoomTypes;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 숙소 상세 조회 전용 ReadManager.
 * 5개 QueryPort를 하나의 readOnly 트랜잭션에서 조회한다.
 * 1:N 관계가 다수이므로 조인이 아닌 별도 쿼리로 조회 (카테시안 곱 방지).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class PropertyDetailReadManager {

    private final PropertyQueryPort propertyQueryPort;
    private final PropertyPhotoQueryPort photoQueryPort;
    private final PropertyAmenityQueryPort amenityQueryPort;
    private final PropertyAttributeValueQueryPort attributeValueQueryPort;
    private final RoomTypeQueryPort roomTypeQueryPort;

    public PropertyDetailReadManager(PropertyQueryPort propertyQueryPort,
                                      PropertyPhotoQueryPort photoQueryPort,
                                      PropertyAmenityQueryPort amenityQueryPort,
                                      PropertyAttributeValueQueryPort attributeValueQueryPort,
                                      RoomTypeQueryPort roomTypeQueryPort) {
        this.propertyQueryPort = propertyQueryPort;
        this.photoQueryPort = photoQueryPort;
        this.amenityQueryPort = amenityQueryPort;
        this.attributeValueQueryPort = attributeValueQueryPort;
        this.roomTypeQueryPort = roomTypeQueryPort;
    }

    @Transactional(readOnly = true)
    public PropertyDetailBundle getById(PropertyId propertyId) {
        Property property = propertyQueryPort.findById(propertyId)
            .orElseThrow(PropertyNotFoundException::new);

        PropertyPhotos photos = PropertyPhotos.reconstitute(
            photoQueryPort.findByPropertyId(propertyId));

        PropertyAmenities amenities = amenityQueryPort.findByPropertyId(propertyId);

        PropertyAttributeValues attributeValues = attributeValueQueryPort.findByPropertyId(propertyId);

        RoomTypes roomTypes = RoomTypes.from(roomTypeQueryPort.findByPropertyId(propertyId));

        return new PropertyDetailBundle(property, photos, amenities, attributeValues, roomTypes);
    }
}
