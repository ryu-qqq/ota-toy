package com.ryuqq.otatoy.api.extranet.property.mapper;

import com.ryuqq.otatoy.api.extranet.property.dto.request.RegisterPropertyApiRequest;
import com.ryuqq.otatoy.application.property.dto.command.RegisterPropertyCommand;
import com.ryuqq.otatoy.domain.brand.BrandId;
import com.ryuqq.otatoy.domain.partner.PartnerId;
import com.ryuqq.otatoy.domain.property.Location;
import com.ryuqq.otatoy.domain.property.PromotionText;
import com.ryuqq.otatoy.domain.property.PropertyDescription;
import com.ryuqq.otatoy.domain.property.PropertyName;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;

/**
 * API Request DTO를 Application Command로 변환하는 매퍼.
 * 원시 타입(Long, String 등)을 Domain VO로 변환하는 책임을 전담한다 (AC-5).
 * <p>
 * Controller에서 인라인 변환을 하지 않고 반드시 이 매퍼를 사용한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public final class PropertyApiMapper {

    private PropertyApiMapper() {}

    /**
     * 숙소 등록 API 요청을 Application Command로 변환한다.
     * nullable 필드(brandId, description, promotionText)는 null일 경우 Domain VO를 생성하지 않는다.
     */
    public static RegisterPropertyCommand toCommand(RegisterPropertyApiRequest request) {
        return RegisterPropertyCommand.of(
            PartnerId.of(request.partnerId()),
            request.brandId() != null ? BrandId.of(request.brandId()) : null,
            PropertyTypeId.of(request.propertyTypeId()),
            PropertyName.of(request.name()),
            request.description() != null ? PropertyDescription.of(request.description()) : null,
            Location.of(request.address(), request.latitude(), request.longitude(),
                request.neighborhood(), request.region()),
            request.promotionText() != null ? PromotionText.of(request.promotionText()) : null
        );
    }
}
