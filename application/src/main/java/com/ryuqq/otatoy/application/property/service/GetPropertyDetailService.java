package com.ryuqq.otatoy.application.property.service;

import com.ryuqq.otatoy.application.property.assembler.PropertySearchResultAssembler;
import com.ryuqq.otatoy.application.property.dto.PropertyDetailBundle;
import com.ryuqq.otatoy.application.property.dto.result.PropertyDetail;
import com.ryuqq.otatoy.application.property.manager.PropertyDetailReadManager;
import com.ryuqq.otatoy.application.property.port.in.GetPropertyDetailUseCase;
import com.ryuqq.otatoy.domain.property.PropertyId;

import org.springframework.stereotype.Service;

@Service
public class GetPropertyDetailService implements GetPropertyDetailUseCase {

    private final PropertyDetailReadManager detailReadManager;
    private final PropertySearchResultAssembler assembler;

    public GetPropertyDetailService(PropertyDetailReadManager detailReadManager,
                                     PropertySearchResultAssembler assembler) {
        this.detailReadManager = detailReadManager;
        this.assembler = assembler;
    }

    @Override
    public PropertyDetail execute(PropertyId propertyId) {
        PropertyDetailBundle bundle = detailReadManager.getById(propertyId);
        return assembler.toDetail(bundle);
    }
}
