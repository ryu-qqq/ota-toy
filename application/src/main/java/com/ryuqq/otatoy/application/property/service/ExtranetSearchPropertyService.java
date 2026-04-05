package com.ryuqq.otatoy.application.property.service;

import com.ryuqq.otatoy.application.common.dto.SliceResult;
import com.ryuqq.otatoy.application.property.assembler.PropertySearchResultAssembler;
import com.ryuqq.otatoy.application.property.dto.query.ExtranetSearchPropertyQuery;
import com.ryuqq.otatoy.application.property.dto.result.ExtranetPropertySliceResult;
import com.ryuqq.otatoy.application.property.factory.PropertySearchCriteriaFactory;
import com.ryuqq.otatoy.application.property.manager.PropertySearchReadManager;
import com.ryuqq.otatoy.application.property.port.in.ExtranetSearchPropertyUseCase;
import com.ryuqq.otatoy.domain.property.ExtranetPropertySliceCriteria;
import com.ryuqq.otatoy.domain.property.Property;

import org.springframework.stereotype.Service;

@Service
public class ExtranetSearchPropertyService implements ExtranetSearchPropertyUseCase {

    private final PropertySearchCriteriaFactory criteriaFactory;
    private final PropertySearchReadManager propertySearchReadManager;
    private final PropertySearchResultAssembler assembler;

    public ExtranetSearchPropertyService(PropertySearchCriteriaFactory criteriaFactory,
                                          PropertySearchReadManager propertySearchReadManager,
                                          PropertySearchResultAssembler assembler) {
        this.criteriaFactory = criteriaFactory;
        this.propertySearchReadManager = propertySearchReadManager;
        this.assembler = assembler;
    }

    @Override
    public ExtranetPropertySliceResult execute(ExtranetSearchPropertyQuery query) {
        ExtranetPropertySliceCriteria criteria = criteriaFactory.createForExtranet(query);
        SliceResult<Property> domainResult = propertySearchReadManager.searchByCriteria(criteria);
        return assembler.toExtranetResult(domainResult);
    }
}
