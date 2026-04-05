package com.ryuqq.otatoy.application.property.factory;

import com.ryuqq.otatoy.application.common.factory.TimeProvider;
import com.ryuqq.otatoy.application.property.dto.command.SetPropertyAttributesCommand;
import com.ryuqq.otatoy.domain.property.PropertyAttributeValue;
import com.ryuqq.otatoy.domain.property.PropertyAttributeValues;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * PropertyAttributeValue 도메인 객체 생성 팩토리.
 * TimeProvider를 주입받아 시간을 일원화한다 (APP-FAC-001).
 * Command 필드가 이미 Domain VO이므로 변환 없이 직접 전달한다 (APP-DTO-001).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class PropertyAttributeValueFactory {

    private final TimeProvider timeProvider;

    public PropertyAttributeValueFactory(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    /**
     * Command로부터 PropertyAttributeValues 일급 컬렉션을 생성한다.
     * 각 AttributeItem을 PropertyAttributeValue.forNew로 변환한다.
     */
    public PropertyAttributeValues create(SetPropertyAttributesCommand command) {
        Instant now = timeProvider.now();

        List<PropertyAttributeValue> items = command.attributes().stream()
            .map(attr -> PropertyAttributeValue.forNew(
                command.propertyId(),
                attr.propertyTypeAttributeId(),
                attr.value(),
                now
            ))
            .toList();

        return PropertyAttributeValues.forNew(items);
    }
}
