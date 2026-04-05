package com.ryuqq.otatoy.application.pricing.port.in;

import com.ryuqq.otatoy.application.pricing.dto.query.FetchRateQuery;
import com.ryuqq.otatoy.application.pricing.dto.result.PropertyRateResult;

/**
 * 요금 조회 UseCase (Port-In).
 * GET /api/v1/properties/{id}/rates 엔드포인트에서 호출한다 (STORY-202).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface FetchRateUseCase {

    PropertyRateResult execute(FetchRateQuery query);
}
