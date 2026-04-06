package com.ryuqq.otatoy.api.customer.rate.controller;

import com.ryuqq.otatoy.api.core.ApiResponse;
import com.ryuqq.otatoy.api.customer.rate.CustomerRateEndpoints;
import com.ryuqq.otatoy.api.customer.rate.dto.request.FetchRateApiRequest;
import com.ryuqq.otatoy.api.customer.rate.dto.response.RoomRateApiResponse;
import com.ryuqq.otatoy.api.customer.rate.mapper.RateApiMapper;
import com.ryuqq.otatoy.application.pricing.dto.result.CustomerPropertyRateResult;
import com.ryuqq.otatoy.application.pricing.port.in.CustomerGetRateUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 고객 요금 조회 Controller.
 * Query(읽기) 전용 — GET 엔드포인트만 포함한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@RestController
@RequestMapping(CustomerRateEndpoints.BASE)
@Tag(name = "Customer - 요금 조회", description = "고객용 숙소 요금 조회 API")
public class CustomerRateQueryController {

    private final CustomerGetRateUseCase customerGetRateUseCase;

    public CustomerRateQueryController(CustomerGetRateUseCase customerGetRateUseCase) {
        this.customerGetRateUseCase = customerGetRateUseCase;
    }

    @Operation(summary = "숙소 요금 조회", description = "특정 숙소의 객실별 요금을 조회합니다")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검증 실패")
    })
    @GetMapping(CustomerRateEndpoints.RATES)
    public ResponseEntity<ApiResponse<List<RoomRateApiResponse>>> getRates(
            @Parameter(description = "숙소 ID") @PathVariable(CustomerRateEndpoints.PATH_PROPERTY_ID) Long propertyId,
            @Valid FetchRateApiRequest request) {

        CustomerPropertyRateResult result = customerGetRateUseCase.execute(
                RateApiMapper.toQuery(propertyId, request.checkIn(), request.checkOut(), request.guests()));

        List<RoomRateApiResponse> response = result.roomRates().stream()
                .map(RateApiMapper::toApiResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.of(response));
    }
}
