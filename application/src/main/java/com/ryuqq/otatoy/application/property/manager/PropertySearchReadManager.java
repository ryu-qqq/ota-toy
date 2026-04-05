package com.ryuqq.otatoy.application.property.manager;

import com.ryuqq.otatoy.application.common.dto.SliceResult;
import com.ryuqq.otatoy.application.property.port.out.PropertyQueryPort;
import com.ryuqq.otatoy.application.property.port.out.PropertySearchQueryPort;
import com.ryuqq.otatoy.domain.property.ExtranetPropertySliceCriteria;
import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertySliceCriteria;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 고객 숙소 검색 전용 ReadManager.
 * 크로스 BC 조인 쿼리의 트랜잭션 경계를 관리한다.
 * 읽기 전용 트랜잭션으로 조회를 담당한다 (APP-MGR-001).
 * 도메인 객체만 반환한다 — Application DTO 변환은 Assembler가 담당한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class PropertySearchReadManager {

    private final PropertySearchQueryPort propertySearchQueryPort;
    private final PropertyQueryPort propertyQueryPort;

    public PropertySearchReadManager(PropertySearchQueryPort propertySearchQueryPort,
                                      PropertyQueryPort propertyQueryPort) {
        this.propertySearchQueryPort = propertySearchQueryPort;
        this.propertyQueryPort = propertyQueryPort;
    }

    @Transactional(readOnly = true)
    public SliceResult<Property> searchByCondition(PropertySliceCriteria criteria) {
        return propertySearchQueryPort.searchByCondition(criteria);
    }

    @Transactional(readOnly = true)
    public SliceResult<Property> searchByCriteria(ExtranetPropertySliceCriteria criteria) {
        return propertyQueryPort.findByCriteria(criteria);
    }
}
