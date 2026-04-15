package com.ryuqq.otatoy.application.property.port.out;

import com.ryuqq.otatoy.application.common.dto.SliceResult;
import com.ryuqq.otatoy.domain.property.ExtranetPropertySliceCriteria;
import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertyId;

import java.util.Optional;

/**
 * Property 조회 전용 Outbound Port.
 * Adapter(persistence-mysql)에서 구현한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public interface PropertyQueryPort {

    Optional<Property> findById(PropertyId id);

    boolean existsById(PropertyId id);

    /**
     * 파트너 숙소 목록을 커서 기반으로 조회한다.
     */
    SliceResult<Property> findByCriteria(ExtranetPropertySliceCriteria criteria);
}
