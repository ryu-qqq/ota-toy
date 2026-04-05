package com.ryuqq.otatoy.api.extranet.property.mapper;

import com.ryuqq.otatoy.application.property.dto.query.ExtranetSearchPropertyQuery;
import com.ryuqq.otatoy.domain.partner.PartnerId;
import com.ryuqq.otatoy.domain.property.PropertyId;

/**
 * 숙소 조회 API 요청 파라미터를 Application Query/Domain VO로 변환하는 매퍼.
 * 원시 타입(Long, int 등)을 Domain VO로 변환하는 책임을 전담한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public final class PropertyQueryApiMapper {

    private PropertyQueryApiMapper() {}

    /**
     * 파트너 숙소 목록 조회 API 파라미터를 ExtranetSearchPropertyQuery로 변환한다.
     */
    public static ExtranetSearchPropertyQuery toExtranetSearchQuery(Long partnerId, int size, Long cursor) {
        return new ExtranetSearchPropertyQuery(
            PartnerId.of(partnerId),
            size,
            cursor
        );
    }

    /**
     * 숙소 ID 원시값을 PropertyId VO로 변환한다.
     */
    public static PropertyId toPropertyId(Long propertyId) {
        return PropertyId.of(propertyId);
    }
}
