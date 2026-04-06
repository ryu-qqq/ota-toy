package com.ryuqq.otatoy.application.property.factory;

import com.ryuqq.otatoy.application.property.dto.command.RegisterPropertyCommand;
import com.ryuqq.otatoy.application.supplier.dto.SupplierPropertyData;
import com.ryuqq.otatoy.domain.partner.PartnerId;
import com.ryuqq.otatoy.domain.property.Location;
import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertyDescription;
import com.ryuqq.otatoy.domain.property.PropertyName;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;
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
    /**
     * 외부 공급자 데이터로부터 Property를 생성한다.
     * 공급자 연동 Property는 partnerId 없이 생성되므로 시스템 PartnerId(0)을 사용한다.
     */
    public Property createFromSupplier(SupplierPropertyData data) {
        Instant now = timeProvider.now();
        return Property.forNew(
                PartnerId.of(0L),
                null,
                PropertyTypeId.of(1L),
                PropertyName.of(data.name()),
                PropertyDescription.of(data.description()),
                Location.of(data.address(), data.latitude(), data.longitude(), null, null),
                null,
                now
        );
    }

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
