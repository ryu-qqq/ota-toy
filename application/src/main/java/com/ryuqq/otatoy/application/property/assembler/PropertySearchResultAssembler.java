package com.ryuqq.otatoy.application.property.assembler;

import com.ryuqq.otatoy.application.common.dto.SliceResult;
import com.ryuqq.otatoy.application.property.dto.PropertyDetailBundle;
import com.ryuqq.otatoy.application.property.dto.result.CustomerPropertySliceResult;
import com.ryuqq.otatoy.application.property.dto.result.ExtranetPropertySliceResult;
import com.ryuqq.otatoy.application.property.dto.result.PropertyDetail;
import com.ryuqq.otatoy.application.property.dto.result.PropertySummary;
import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.property.Property;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 숙소 검색 결과 조립 Assembler.
 * 도메인 객체(Property) → Application DTO(PropertySummary, SliceResult) 변환을 담당한다.
 * Service에서 직접 변환하지 않고 Assembler를 통해 변환한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class PropertySearchResultAssembler {

    /**
     * 고객 검색 결과를 CustomerPropertySliceResult로 변환한다.
     * 최저 가격은 현재 0으로 설정 — 추후 Rate 연동 시 실제 최저 가격을 계산한다.
     */
    public CustomerPropertySliceResult toCustomerResult(SliceResult<Property> domainResult) {
        List<PropertySummary> summaries = domainResult.content().stream()
                .map(property -> PropertySummary.of(property, Money.of(0)))
                .toList();

        return CustomerPropertySliceResult.of(summaries, domainResult.sliceMeta());
    }

    /**
     * 파트너 숙소 목록 결과를 ExtranetPropertySliceResult로 변환한다.
     * 최저 가격은 현재 0으로 설정 — 추후 Rate 연동 시 실제 최저 가격을 계산한다.
     */
    public ExtranetPropertySliceResult toExtranetResult(SliceResult<Property> domainResult) {
        List<PropertySummary> summaries = domainResult.content().stream()
                .map(property -> PropertySummary.of(property, Money.of(0)))
                .toList();

        return ExtranetPropertySliceResult.of(summaries, domainResult.sliceMeta());
    }

    /**
     * 도메인 번들을 PropertyDetail로 변환한다.
     */
    public PropertyDetail toDetail(PropertyDetailBundle bundle) {
        return PropertyDetail.of(
            bundle.property(),
            bundle.photos(),
            bundle.amenities(),
            bundle.attributeValues(),
            bundle.roomTypes()
        );
    }
}
