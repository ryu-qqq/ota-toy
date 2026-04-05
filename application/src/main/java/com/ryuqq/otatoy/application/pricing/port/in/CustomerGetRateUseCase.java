package com.ryuqq.otatoy.application.pricing.port.in;

import com.ryuqq.otatoy.application.pricing.dto.query.CustomerGetRateQuery;
import com.ryuqq.otatoy.application.pricing.dto.result.CustomerPropertyRateResult;

/**
 * 고객 요금 조회 UseCase (Port-In).
 * GET /api/v1/properties/{id}/rates 엔드포인트에서 호출한다.
 * 관리자/파트너 요금 조회와 구분하기 위해 Customer 접두사를 사용한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface CustomerGetRateUseCase {

    CustomerPropertyRateResult execute(CustomerGetRateQuery query);
}
