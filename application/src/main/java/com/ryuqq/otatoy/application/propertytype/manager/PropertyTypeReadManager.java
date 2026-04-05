package com.ryuqq.otatoy.application.propertytype.manager;

import com.ryuqq.otatoy.application.propertytype.port.out.PropertyTypeQueryPort;
import com.ryuqq.otatoy.domain.propertytype.PropertyType;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeAttribute;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeAttributeId;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeNotFoundException;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * PropertyType 조회 트랜잭션 경계 관리자.
 * 다른 BC에서도 호출 가능한 ReadManager (APP-BC-001).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class PropertyTypeReadManager {

    private final PropertyTypeQueryPort propertyTypeQueryPort;

    public PropertyTypeReadManager(PropertyTypeQueryPort propertyTypeQueryPort) {
        this.propertyTypeQueryPort = propertyTypeQueryPort;
    }

    @Transactional(readOnly = true)
    public PropertyType getById(PropertyTypeId id) {
        return propertyTypeQueryPort.findById(id)
            .orElseThrow(PropertyTypeNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public void verifyExists(PropertyTypeId id) {
        if (!propertyTypeQueryPort.existsById(id)) {
            throw new PropertyTypeNotFoundException();
        }
    }

    /**
     * 해당 숙소 유형에 정의된 필수(required=true) 속성 ID 목록을 조회한다.
     */
    @Transactional(readOnly = true)
    public Set<PropertyTypeAttributeId> getRequiredAttributeIds(PropertyTypeId propertyTypeId) {
        List<PropertyTypeAttribute> attributes = propertyTypeQueryPort.findAttributesByPropertyTypeId(propertyTypeId);
        return attributes.stream()
            .filter(PropertyTypeAttribute::required)
            .map(PropertyTypeAttribute::id)
            .collect(Collectors.toSet());
    }
}
