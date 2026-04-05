package com.ryuqq.otatoy.application.property.manager;

import com.ryuqq.otatoy.application.property.port.out.PropertyQueryPort;
import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.property.PropertyNotFoundException;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Property 조회 트랜잭션 경계 관리자.
 * 읽기 전용 트랜잭션으로 조회를 담당한다 (APP-MGR-001).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class PropertyReadManager {

    private final PropertyQueryPort propertyQueryPort;

    public PropertyReadManager(PropertyQueryPort propertyQueryPort) {
        this.propertyQueryPort = propertyQueryPort;
    }

    @Transactional(readOnly = true)
    public Property getById(PropertyId id) {
        return propertyQueryPort.findById(id)
            .orElseThrow(PropertyNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public void verifyExists(PropertyId id) {
        if (!propertyQueryPort.existsById(id)) {
            throw new PropertyNotFoundException();
        }
    }

}
