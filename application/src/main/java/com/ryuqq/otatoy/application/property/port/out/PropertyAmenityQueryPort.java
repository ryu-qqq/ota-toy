package com.ryuqq.otatoy.application.property.port.out;

import com.ryuqq.otatoy.domain.property.PropertyAmenities;
import com.ryuqq.otatoy.domain.property.PropertyId;

/**
 * PropertyAmenity 조회 전용 Outbound Port.
 * 해당 숙소의 활성(삭제되지 않은) 편의시설을 조회한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public interface PropertyAmenityQueryPort {

    /**
     * 해당 숙소의 활성 편의시설을 조회한다.
     */
    PropertyAmenities findByPropertyId(PropertyId propertyId);
}
