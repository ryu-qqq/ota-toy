package com.ryuqq.otatoy.application.property.port.in;

import com.ryuqq.otatoy.application.property.dto.query.ExtranetSearchPropertyQuery;
import com.ryuqq.otatoy.application.property.dto.result.ExtranetPropertySliceResult;

/**
 * 파트너 숙소 목록 조회 UseCase (Port-In).
 * GET /api/v1/extranet/properties 엔드포인트에서 호출한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface ExtranetSearchPropertyUseCase {

    ExtranetPropertySliceResult execute(ExtranetSearchPropertyQuery query);
}
