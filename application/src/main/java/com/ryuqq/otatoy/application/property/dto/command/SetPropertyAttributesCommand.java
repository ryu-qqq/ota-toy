package com.ryuqq.otatoy.application.property.dto.command;

import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeAttributeId;

import java.util.List;

/**
 * 숙소 속성값 설정 요청 Command.
 * 필드 타입은 Domain VO를 사용한다 (APP-DTO-001).
 * 전체 교체(replace) 방식으로 기존 속성값을 모두 대체한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public record SetPropertyAttributesCommand(
    PropertyId propertyId,
    List<AttributeItem> attributes
) {

    /**
     * 개별 속성값 항목.
     * PropertyTypeAttribute에 정의된 속성 ID와 실제 값을 매핑한다.
     */
    public record AttributeItem(
        PropertyTypeAttributeId propertyTypeAttributeId,
        String value
    ) {

        public static AttributeItem of(PropertyTypeAttributeId propertyTypeAttributeId, String value) {
            return new AttributeItem(propertyTypeAttributeId, value);
        }
    }

    public static SetPropertyAttributesCommand of(PropertyId propertyId, List<AttributeItem> attributes) {
        return new SetPropertyAttributesCommand(propertyId, attributes);
    }
}
