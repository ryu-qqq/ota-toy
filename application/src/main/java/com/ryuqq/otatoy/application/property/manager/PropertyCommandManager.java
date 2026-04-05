package com.ryuqq.otatoy.application.property.manager;

import com.ryuqq.otatoy.application.property.port.out.PropertyCommandPort;
import com.ryuqq.otatoy.domain.property.Property;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Property 저장 트랜잭션 경계 관리자.
 * 단일 Aggregate 저장을 담당한다 (APP-MGR-001).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class PropertyCommandManager {

    private final PropertyCommandPort propertyCommandPort;

    public PropertyCommandManager(PropertyCommandPort propertyCommandPort) {
        this.propertyCommandPort = propertyCommandPort;
    }

    @Transactional
    public Long persist(Property property) {
        return propertyCommandPort.persist(property);
    }

    @Transactional
    public void persistAll(List<Property> properties) {
        propertyCommandPort.persistAll(properties);
    }
}
