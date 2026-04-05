package com.ryuqq.otatoy.application.property.dto.command;

import com.ryuqq.otatoy.domain.brand.BrandId;
import com.ryuqq.otatoy.domain.partner.PartnerId;
import com.ryuqq.otatoy.domain.property.Location;
import com.ryuqq.otatoy.domain.property.PromotionText;
import com.ryuqq.otatoy.domain.property.PropertyDescription;
import com.ryuqq.otatoy.domain.property.PropertyName;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;

/**
 * 숙소 기본정보 등록 요청 Command.
 * 필드 타입은 Domain VO를 사용한다 (APP-DTO-001).
 * 편의시설/사진/속성값은 각각 독립 Command로 분리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public record RegisterPropertyCommand(
    PartnerId partnerId,
    BrandId brandId,
    PropertyTypeId propertyTypeId,
    PropertyName name,
    PropertyDescription description,
    Location location,
    PromotionText promotionText
) {

    public static RegisterPropertyCommand of(PartnerId partnerId, BrandId brandId,
                                              PropertyTypeId propertyTypeId,
                                              PropertyName name, PropertyDescription description,
                                              Location location, PromotionText promotionText) {
        return new RegisterPropertyCommand(partnerId, brandId, propertyTypeId, name, description,
            location, promotionText);
    }
}
