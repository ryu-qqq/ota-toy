package com.ryuqq.otatoy.api.extranet.property.mapper;

import com.ryuqq.otatoy.api.extranet.property.dto.SetPropertyAttributesApiRequest;
import com.ryuqq.otatoy.application.property.dto.command.SetPropertyAttributesCommand;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeAttributeId;

/**
 * 속성값 API Request DTO를 Application Command로 변환하는 매퍼.
 * 원시 타입(Long, String)을 Domain VO로 변환하는 책임을 전담한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public final class PropertyAttributeApiMapper {

    private PropertyAttributeApiMapper() {}

    /**
     * 속성값 설정 API 요청을 Application Command로 변환한다.
     *
     * @param propertyId 숙소 ID (PathVariable)
     * @param request    속성값 설정 요청 DTO
     * @return 속성값 설정 Command
     */
    public static SetPropertyAttributesCommand toCommand(Long propertyId, SetPropertyAttributesApiRequest request) {
        return SetPropertyAttributesCommand.of(
            PropertyId.of(propertyId),
            request.attributes().stream()
                .map(attr -> SetPropertyAttributesCommand.AttributeItem.of(
                    PropertyTypeAttributeId.of(attr.propertyTypeAttributeId()),
                    attr.value()
                ))
                .toList()
        );
    }
}
