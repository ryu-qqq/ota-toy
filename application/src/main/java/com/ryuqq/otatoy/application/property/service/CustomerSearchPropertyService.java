package com.ryuqq.otatoy.application.property.service;

import com.ryuqq.otatoy.application.common.dto.SliceResult;
import com.ryuqq.otatoy.application.property.assembler.PropertySearchResultAssembler;
import com.ryuqq.otatoy.application.property.dto.query.CustomerSearchPropertyQuery;
import com.ryuqq.otatoy.application.property.dto.result.CustomerPropertySliceResult;
import com.ryuqq.otatoy.application.property.factory.PropertySearchCriteriaFactory;
import com.ryuqq.otatoy.application.property.manager.PropertySearchReadManager;
import com.ryuqq.otatoy.application.property.port.in.CustomerSearchPropertyUseCase;
import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertySliceCriteria;

import org.springframework.stereotype.Service;

/**
 * 고객 숙소 검색 Service.
 * UseCase 구현체로서 오케스트레이션만 담당한다 (APP-SVC-001).
 * @Transactional 금지 — 트랜잭션 경계는 Manager에서 관리한다.
 * CriteriaFactory → ReadManager → Assembler 흐름으로 조립한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Service
public class CustomerSearchPropertyService implements CustomerSearchPropertyUseCase {

    private final PropertySearchCriteriaFactory criteriaFactory;
    private final PropertySearchReadManager propertySearchReadManager;
    private final PropertySearchResultAssembler assembler;

    public CustomerSearchPropertyService(PropertySearchCriteriaFactory criteriaFactory,
                                          PropertySearchReadManager propertySearchReadManager,
                                          PropertySearchResultAssembler assembler) {
        this.criteriaFactory = criteriaFactory;
        this.propertySearchReadManager = propertySearchReadManager;
        this.assembler = assembler;
    }

    @Override
    public CustomerPropertySliceResult execute(CustomerSearchPropertyQuery query) {
        PropertySliceCriteria criteria = criteriaFactory.create(query);
        SliceResult<Property> domainResult = propertySearchReadManager.searchByCondition(criteria);
        return assembler.toCustomerResult(domainResult);
    }
}
