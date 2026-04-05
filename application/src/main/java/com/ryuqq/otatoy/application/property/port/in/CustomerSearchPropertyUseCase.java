package com.ryuqq.otatoy.application.property.port.in;

import com.ryuqq.otatoy.application.property.dto.query.CustomerSearchPropertyQuery;
import com.ryuqq.otatoy.application.property.dto.result.CustomerPropertySliceResult;

/**
 * 고객 숙소 검색 UseCase (Port-In).
 * GET /api/v1/search/properties 엔드포인트에서 호출한다.
 * 관리자/파트너 검색과 구분하기 위해 Customer 접두사를 사용한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface CustomerSearchPropertyUseCase {

    CustomerPropertySliceResult execute(CustomerSearchPropertyQuery query);
}
