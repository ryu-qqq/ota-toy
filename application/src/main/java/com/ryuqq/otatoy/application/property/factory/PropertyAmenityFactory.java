package com.ryuqq.otatoy.application.property.factory;

import com.ryuqq.otatoy.application.common.factory.TimeProvider;
import com.ryuqq.otatoy.application.property.dto.command.SetPropertyAmenitiesCommand;
import com.ryuqq.otatoy.domain.property.PropertyAmenities;
import com.ryuqq.otatoy.domain.property.PropertyAmenity;
import com.ryuqq.otatoy.domain.property.PropertyId;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * PropertyAmenity 도메인 객체 생성 팩토리.
 * TimeProvider를 주입받아 시간을 일원화한다 (APP-FAC-001).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class PropertyAmenityFactory {

    private final TimeProvider timeProvider;

    public PropertyAmenityFactory(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    /**
     * SetPropertyAmenitiesCommand로부터 PropertyAmenities 일급 컬렉션을 생성한다.
     */
    public PropertyAmenities createAmenities(SetPropertyAmenitiesCommand command) {
        PropertyId propertyId = command.propertyId();
        Instant now = timeProvider.now();

        List<PropertyAmenity> items = command.amenities().stream()
            .map(a -> PropertyAmenity.forNew(
                propertyId,
                a.amenityType(),
                a.name(),
                a.additionalPrice(),
                a.sortOrder(),
                now))
            .toList();

        return PropertyAmenities.forNew(items);
    }
}
