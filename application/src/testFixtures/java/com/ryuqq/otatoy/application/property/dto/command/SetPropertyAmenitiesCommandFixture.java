package com.ryuqq.otatoy.application.property.dto.command;

import com.ryuqq.otatoy.domain.accommodation.AmenityName;
import com.ryuqq.otatoy.domain.accommodation.AmenityType;
import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.property.PropertyId;

import java.util.List;

/**
 * SetPropertyAmenitiesCommand 테스트용 Fixture.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public final class SetPropertyAmenitiesCommandFixture {

    private SetPropertyAmenitiesCommandFixture() {}

    /**
     * 기본 편의시설 설정 커맨드 (와이파이 + 주차장)
     */
    public static SetPropertyAmenitiesCommand aSetPropertyAmenitiesCommand() {
        return SetPropertyAmenitiesCommand.of(
            PropertyId.of(1L),
            List.of(
                SetPropertyAmenitiesCommand.AmenityItem.of(AmenityType.WIFI, AmenityName.of("와이파이"), Money.of(0), 1),
                SetPropertyAmenitiesCommand.AmenityItem.of(AmenityType.PARKING, AmenityName.of("주차장"), Money.of(5000), 2)
            )
        );
    }
}
