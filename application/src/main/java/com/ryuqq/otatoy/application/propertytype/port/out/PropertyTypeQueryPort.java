package com.ryuqq.otatoy.application.propertytype.port.out;

import com.ryuqq.otatoy.domain.propertytype.PropertyType;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeAttribute;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;

import java.util.List;
import java.util.Optional;

/**
 * PropertyType 조회 전용 Outbound Port.
 * Adapter(persistence-mysql)에서 구현한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public interface PropertyTypeQueryPort {

    Optional<PropertyType> findById(PropertyTypeId id);

    boolean existsById(PropertyTypeId id);

    /**
     * 해당 숙소 유형에 정의된 속성 목록을 조회한다.
     */
    List<PropertyTypeAttribute> findAttributesByPropertyTypeId(PropertyTypeId propertyTypeId);
}
