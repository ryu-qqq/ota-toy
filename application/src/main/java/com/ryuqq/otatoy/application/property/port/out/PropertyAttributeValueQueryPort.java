package com.ryuqq.otatoy.application.property.port.out;

import com.ryuqq.otatoy.domain.property.PropertyAttributeValues;
import com.ryuqq.otatoy.domain.property.PropertyId;

/**
 * PropertyAttributeValue 조회 전용 Outbound Port.
 * 해당 숙소의 활성(삭제되지 않은) 속성값을 조회한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public interface PropertyAttributeValueQueryPort {

    /**
     * 해당 숙소의 활성 속성값을 조회한다.
     */
    PropertyAttributeValues findByPropertyId(PropertyId propertyId);
}
