package com.ryuqq.otatoy.api.extranet.property.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 숙소 속성값 설정 요청 DTO.
 * 전체 교체(replace) 방식으로 기존 속성값을 모두 대체한다.
 * 원시 타입으로 수신하며, {@link com.ryuqq.otatoy.api.extranet.property.mapper.PropertyAttributeApiMapper}에서
 * Domain VO로 변환한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public record SetPropertyAttributesApiRequest(

    @NotEmpty(message = "속성값 목록은 필수입니다")
    List<@Valid AttributeItem> attributes
) {

    /**
     * 개별 속성값 항목.
     *
     * @param propertyTypeAttributeId 숙소유형 속성 정의 ID
     * @param value                   속성값
     */
    public record AttributeItem(

        @NotNull(message = "속성 ID는 필수입니다")
        Long propertyTypeAttributeId,

        @NotBlank(message = "속성값은 필수입니다")
        String value
    ) {}
}
