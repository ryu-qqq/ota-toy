package com.ryuqq.otatoy.application.property.port.in;

import com.ryuqq.otatoy.application.common.dto.SliceResult;
import com.ryuqq.otatoy.application.property.dto.query.SearchPropertyQuery;
import com.ryuqq.otatoy.application.property.dto.result.PropertySummary;

/**
 * 고객 숙소 검색 UseCase (Port-In).
 * GET /api/v1/search/properties 엔드포인트에서 호출한다 (STORY-201).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface SearchPropertyUseCase {

    SliceResult<PropertySummary> execute(SearchPropertyQuery query);
}
