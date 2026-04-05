package com.ryuqq.otatoy.application.property.manager;

import com.ryuqq.otatoy.application.property.port.out.PropertyAmenityCommandPort;
import com.ryuqq.otatoy.domain.property.PropertyAmenity;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * PropertyAmenity 저장 트랜잭션 경계 관리자.
 * diff 기반 persist를 담당한다 (APP-MGR-001).
 * @Transactional은 메서드 단위로 선언한다. 클래스 레벨 금지.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class PropertyAmenityCommandManager {

    private final PropertyAmenityCommandPort propertyAmenityCommandPort;

    public PropertyAmenityCommandManager(PropertyAmenityCommandPort propertyAmenityCommandPort) {
        this.propertyAmenityCommandPort = propertyAmenityCommandPort;
    }

    /**
     * 편의시설 목록을 일괄 저장한다.
     * id가 null이면 INSERT, 있으면 merge(UPDATE) 처리.
     */
    @Transactional
    public void persistAll(List<PropertyAmenity> amenities) {
        if (amenities != null && !amenities.isEmpty()) {
            propertyAmenityCommandPort.persistAll(amenities);
        }
    }
}
