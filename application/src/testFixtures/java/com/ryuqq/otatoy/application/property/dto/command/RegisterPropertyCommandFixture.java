package com.ryuqq.otatoy.application.property.dto.command;

import com.ryuqq.otatoy.domain.brand.BrandId;
import com.ryuqq.otatoy.domain.partner.PartnerId;
import com.ryuqq.otatoy.domain.property.Location;
import com.ryuqq.otatoy.domain.property.PromotionText;
import com.ryuqq.otatoy.domain.property.PropertyDescription;
import com.ryuqq.otatoy.domain.property.PropertyName;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;

/**
 * RegisterPropertyCommand 테스트용 Fixture.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public final class RegisterPropertyCommandFixture {

    private RegisterPropertyCommandFixture() {}

    /**
     * 기본 숙소 등록 커맨드
     */
    public static RegisterPropertyCommand aRegisterPropertyCommand() {
        return RegisterPropertyCommand.of(
            PartnerId.of(1L),
            BrandId.of(10L),
            PropertyTypeId.of(100L),
            PropertyName.of("테스트 호텔"),
            PropertyDescription.of("테스트 설명"),
            Location.of("서울시 강남구", 37.5, 127.0, "강남", "서울"),
            PromotionText.of("특가 이벤트")
        );
    }

    /**
     * 지정 파트너 ID의 숙소 등록 커맨드
     */
    public static RegisterPropertyCommand aRegisterPropertyCommandWithPartnerId(long partnerId) {
        return RegisterPropertyCommand.of(
            PartnerId.of(partnerId),
            BrandId.of(10L),
            PropertyTypeId.of(100L),
            PropertyName.of("테스트 호텔"),
            PropertyDescription.of("테스트 설명"),
            Location.of("서울시 강남구", 37.5, 127.0, "강남", "서울"),
            PromotionText.of("특가 이벤트")
        );
    }
}
