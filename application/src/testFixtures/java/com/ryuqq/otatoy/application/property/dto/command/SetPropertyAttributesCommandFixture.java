package com.ryuqq.otatoy.application.property.dto.command;

import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeAttributeId;

import java.util.List;

/**
 * SetPropertyAttributesCommand 테스트용 Fixture.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public final class SetPropertyAttributesCommandFixture {

    private SetPropertyAttributesCommandFixture() {}

    /**
     * 기본 속성값 설정 커맨드
     */
    public static SetPropertyAttributesCommand aSetPropertyAttributesCommand() {
        return SetPropertyAttributesCommand.of(
            PropertyId.of(1L),
            List.of(
                SetPropertyAttributesCommand.AttributeItem.of(PropertyTypeAttributeId.of(100L), "2성급"),
                SetPropertyAttributesCommand.AttributeItem.of(PropertyTypeAttributeId.of(200L), "14:00")
            )
        );
    }
}
