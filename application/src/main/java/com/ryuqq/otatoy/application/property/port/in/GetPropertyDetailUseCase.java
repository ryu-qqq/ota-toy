package com.ryuqq.otatoy.application.property.port.in;

import com.ryuqq.otatoy.application.property.dto.result.PropertyDetail;
import com.ryuqq.otatoy.domain.property.PropertyId;

/**
 * 숙소 상세 조회 UseCase (Port-In).
 * GET /api/v1/extranet/properties/{propertyId} 엔드포인트에서 호출한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface GetPropertyDetailUseCase {

    PropertyDetail execute(PropertyId propertyId);
}
