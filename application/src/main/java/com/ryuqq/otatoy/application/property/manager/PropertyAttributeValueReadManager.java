package com.ryuqq.otatoy.application.property.manager;

import com.ryuqq.otatoy.application.property.port.out.PropertyAttributeValueQueryPort;
import com.ryuqq.otatoy.domain.property.PropertyAttributeValues;
import com.ryuqq.otatoy.domain.property.PropertyId;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * PropertyAttributeValue 조회 트랜잭션 경계 관리자.
 * 읽기 전용 트랜잭션으로 조회를 담당한다 (APP-MGR-001).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class PropertyAttributeValueReadManager {

    private final PropertyAttributeValueQueryPort propertyAttributeValueQueryPort;

    public PropertyAttributeValueReadManager(PropertyAttributeValueQueryPort propertyAttributeValueQueryPort) {
        this.propertyAttributeValueQueryPort = propertyAttributeValueQueryPort;
    }

    /**
     * 해당 숙소의 활성 속성값을 조회한다.
     */
    @Transactional(readOnly = true)
    public PropertyAttributeValues getByPropertyId(PropertyId propertyId) {
        return propertyAttributeValueQueryPort.findByPropertyId(propertyId);
    }
}
