package com.ryuqq.otatoy.application.property.manager;

import com.ryuqq.otatoy.application.common.dto.SliceResult;
import com.ryuqq.otatoy.application.property.dto.result.PropertySummary;
import com.ryuqq.otatoy.application.property.port.out.PropertySearchQueryPort;
import com.ryuqq.otatoy.domain.property.PropertySliceCriteria;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 고객 숙소 검색 전용 ReadManager.
 * 크로스 BC 조인 쿼리(Property + RoomType + Inventory + Rate)의 트랜잭션 경계를 관리한다.
 * 읽기 전용 트랜잭션으로 조회를 담당한다 (APP-MGR-001).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class PropertySearchReadManager {

    private final PropertySearchQueryPort propertySearchQueryPort;

    public PropertySearchReadManager(PropertySearchQueryPort propertySearchQueryPort) {
        this.propertySearchQueryPort = propertySearchQueryPort;
    }

    /**
     * 검색 조건에 맞는 숙소 목록을 커서 기반으로 조회한다.
     */
    @Transactional(readOnly = true)
    public SliceResult<PropertySummary> searchByCondition(PropertySliceCriteria criteria) {
        return propertySearchQueryPort.searchByCondition(criteria);
    }
}
