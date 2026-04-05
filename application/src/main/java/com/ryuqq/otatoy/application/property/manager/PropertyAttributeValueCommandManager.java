package com.ryuqq.otatoy.application.property.manager;

import com.ryuqq.otatoy.application.property.port.out.PropertyAttributeValueCommandPort;
import com.ryuqq.otatoy.domain.property.PropertyAttributeValue;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * PropertyAttributeValue 저장 트랜잭션 경계 관리자.
 * diff 기반 persist를 담당한다 (APP-MGR-001).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class PropertyAttributeValueCommandManager {

    private final PropertyAttributeValueCommandPort propertyAttributeValueCommandPort;

    public PropertyAttributeValueCommandManager(PropertyAttributeValueCommandPort propertyAttributeValueCommandPort) {
        this.propertyAttributeValueCommandPort = propertyAttributeValueCommandPort;
    }

    /**
     * 속성값 목록을 일괄 저장한다.
     * id가 null이면 INSERT, 있으면 merge(UPDATE) 처리.
     */
    @Transactional
    public void persistAll(List<PropertyAttributeValue> attributeValues) {
        if (attributeValues != null && !attributeValues.isEmpty()) {
            propertyAttributeValueCommandPort.persistAll(attributeValues);
        }
    }
}
