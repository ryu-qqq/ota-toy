package com.ryuqq.otatoy.api.customer.search.controller;

import com.ryuqq.otatoy.api.core.ApiResponse;
import com.ryuqq.otatoy.api.core.SliceResponse;
import com.ryuqq.otatoy.api.customer.search.CustomerSearchEndpoints;
import com.ryuqq.otatoy.api.customer.search.dto.response.PropertySummaryApiResponse;
import com.ryuqq.otatoy.api.customer.search.dto.request.SearchPropertyApiRequest;
import com.ryuqq.otatoy.api.customer.search.mapper.SearchPropertyApiMapper;
import com.ryuqq.otatoy.application.property.dto.result.CustomerPropertySliceResult;
import com.ryuqq.otatoy.application.property.port.in.CustomerSearchPropertyUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 고객 숙소 검색 Controller.
 * Query(읽기) 전용 — GET 엔드포인트만 포함한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@RestController
@RequestMapping(CustomerSearchEndpoints.PROPERTIES)
@Tag(name = "Customer - 숙소 검색", description = "고객용 숙소 검색 API")
public class CustomerSearchQueryController {

    private final CustomerSearchPropertyUseCase customerSearchPropertyUseCase;

    public CustomerSearchQueryController(CustomerSearchPropertyUseCase customerSearchPropertyUseCase) {
        this.customerSearchPropertyUseCase = customerSearchPropertyUseCase;
    }

    @Operation(summary = "숙소 검색", description = "조건에 맞는 숙소를 검색합니다 (커서 기반 페이지네이션)")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "검색 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검증 실패")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<SliceResponse<PropertySummaryApiResponse>>> searchProperties(
            @Valid SearchPropertyApiRequest request) {

        CustomerPropertySliceResult result = customerSearchPropertyUseCase.execute(
                SearchPropertyApiMapper.toQuery(request));

        SliceResponse<PropertySummaryApiResponse> response = SliceResponse.of(
                result.content().stream()
                        .map(SearchPropertyApiMapper::toApiResponse)
                        .toList(),
                result.hasNext(),
                result.nextCursor()
        );

        return ResponseEntity.ok(ApiResponse.of(response));
    }
}
