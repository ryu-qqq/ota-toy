package com.ryuqq.otatoy.api.customer.search;

import com.ryuqq.otatoy.api.core.ApiResponse;
import com.ryuqq.otatoy.api.core.SliceResponse;
import com.ryuqq.otatoy.api.customer.search.dto.PropertySummaryApiResponse;
import com.ryuqq.otatoy.api.customer.search.dto.SearchPropertyApiRequest;
import com.ryuqq.otatoy.api.customer.search.mapper.SearchPropertyApiMapper;
import com.ryuqq.otatoy.application.common.dto.SliceResult;
import com.ryuqq.otatoy.application.property.dto.result.PropertySummary;
import com.ryuqq.otatoy.application.property.port.in.SearchPropertyUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 고객 숙소 검색 Controller.
 * SearchPropertyUseCase만 의존하며, 비즈니스 로직 없이 HTTP 변환만 수행한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@RestController
@RequestMapping("/api/v1/search/properties")
@Tag(name = "Customer - 숙소 검색", description = "고객용 숙소 검색 API")
public class SearchPropertyController {

    private final SearchPropertyUseCase searchPropertyUseCase;

    public SearchPropertyController(SearchPropertyUseCase searchPropertyUseCase) {
        this.searchPropertyUseCase = searchPropertyUseCase;
    }

    @Operation(summary = "숙소 검색", description = "조건에 맞는 숙소를 검색합니다 (커서 기반 페이지네이션)")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "검색 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검증 실패")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<SliceResponse<PropertySummaryApiResponse>>> searchProperties(
            @Valid SearchPropertyApiRequest request) {

        // 1. Request -> Query 변환 (ApiMapper 사용)
        SliceResult<PropertySummary> result = searchPropertyUseCase.execute(
                SearchPropertyApiMapper.toQuery(request));

        // 2. Result -> Response 변환 (ApiMapper 사용)
        SliceResponse<PropertySummaryApiResponse> response = SliceResponse.of(
                result.content().stream()
                        .map(SearchPropertyApiMapper::toApiResponse)
                        .toList(),
                result.hasNext(),
                result.nextCursor()
        );

        // 3. 응답 반환
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
