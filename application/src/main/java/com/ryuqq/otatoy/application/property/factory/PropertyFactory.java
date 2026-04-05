package com.ryuqq.otatoy.application.property.factory;

import com.ryuqq.otatoy.application.property.dto.command.RegisterPropertyCommand;
import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.application.common.factory.TimeProvider;

import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Property 도메인 객체 생성 팩토리.
 * TimeProvider를 주입받아 시간을 일원화한다 (APP-FAC-001).
 * Command 필드가 이미 Domain VO이므로 변환 없이 직접 전달한다 (APP-DTO-001).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class PropertyFactory {

    private final TimeProvider timeProvider;

    public PropertyFactory(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    /**
     * Property 기본정보 도메인 객체를 생성한다.
     * 편의시설/사진/속성값은 별도 UseCase에서 처리하므로 여기서 생성하지 않는다.
     */
    public Property createProperty(RegisterPropertyCommand command) {
        Instant now = timeProvider.now();

        return Property.forNew(
            command.partnerId(),
            command.brandId(),
            command.propertyTypeId(),
            command.name(),
            command.description(),
            command.location(),
            command.promotionText(),
            now
        );
    }
}
