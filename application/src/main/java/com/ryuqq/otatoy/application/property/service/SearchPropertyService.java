package com.ryuqq.otatoy.application.property.service;

import com.ryuqq.otatoy.application.common.dto.SliceResult;
import com.ryuqq.otatoy.application.property.dto.query.SearchPropertyQuery;
import com.ryuqq.otatoy.application.property.dto.result.PropertySummary;
import com.ryuqq.otatoy.application.property.manager.PropertySearchReadManager;
import com.ryuqq.otatoy.application.property.port.in.SearchPropertyUseCase;
import com.ryuqq.otatoy.domain.property.PropertySliceCriteria;

import org.springframework.stereotype.Service;

/**
 * 고객 숙소 검색 Service.
 * UseCase 구현체로서 오케스트레이션만 담당한다 (APP-SVC-001).
 * @Transactional 금지 -- 트랜잭션 경계는 Manager에서 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Service
public class SearchPropertyService implements SearchPropertyUseCase {

    private final PropertySearchReadManager propertySearchReadManager;

    public SearchPropertyService(PropertySearchReadManager propertySearchReadManager) {
        this.propertySearchReadManager = propertySearchReadManager;
    }

    @Override
    public SliceResult<PropertySummary> execute(SearchPropertyQuery query) {
        // 1. Query DTO -> Domain Criteria 변환 (Service에서 직접 수행)
        PropertySliceCriteria criteria = new PropertySliceCriteria(
                query.keyword(), query.region(), query.propertyTypeId(),
                query.checkIn(), query.checkOut(), query.guests(),
                query.minPrice(), query.maxPrice(), query.amenityTypes(),
                query.freeCancellationOnly(), query.starRating(),
                query.sortKey(), query.direction(), query.size(), query.cursor()
        );

        // 2. 크로스 BC 검색 조회 (ReadManager -- readOnly 트랜잭션)
        return propertySearchReadManager.searchByCondition(criteria);
    }
}
