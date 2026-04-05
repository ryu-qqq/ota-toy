package com.ryuqq.otatoy.application.property.manager;

import com.ryuqq.otatoy.application.property.port.out.PropertyAmenityQueryPort;
import com.ryuqq.otatoy.domain.property.PropertyAmenities;
import com.ryuqq.otatoy.domain.property.PropertyId;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * PropertyAmenity 조회 트랜잭션 경계 관리자.
 * 읽기 전용 트랜잭션으로 조회를 담당한다 (APP-MGR-001).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class PropertyAmenityReadManager {

    private final PropertyAmenityQueryPort propertyAmenityQueryPort;

    public PropertyAmenityReadManager(PropertyAmenityQueryPort propertyAmenityQueryPort) {
        this.propertyAmenityQueryPort = propertyAmenityQueryPort;
    }

    /**
     * 해당 숙소의 활성 편의시설을 조회한다.
     */
    @Transactional(readOnly = true)
    public PropertyAmenities getByPropertyId(PropertyId propertyId) {
        return propertyAmenityQueryPort.findByPropertyId(propertyId);
    }
}
