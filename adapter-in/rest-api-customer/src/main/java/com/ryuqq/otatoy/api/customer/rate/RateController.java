package com.ryuqq.otatoy.api.customer.rate;

import com.ryuqq.otatoy.api.core.ApiResponse;
import com.ryuqq.otatoy.api.customer.rate.dto.FetchRateApiRequest;
import com.ryuqq.otatoy.api.customer.rate.dto.RoomRateApiResponse;
import com.ryuqq.otatoy.api.customer.rate.mapper.RateApiMapper;
import com.ryuqq.otatoy.application.pricing.dto.result.PropertyRateResult;
import com.ryuqq.otatoy.application.pricing.port.in.FetchRateUseCase;
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
 * FetchRateUseCase만 의존하며, 비즈니스 로직 없이 HTTP 변환만 수행한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@RestController
@RequestMapping("/api/v1/properties")
@Tag(name = "Customer - 요금 조회", description = "고객용 숙소 요금 조회 API")
public class RateController {

    private final FetchRateUseCase fetchRateUseCase;

    public RateController(FetchRateUseCase fetchRateUseCase) {
        this.fetchRateUseCase = fetchRateUseCase;
    }

    @Operation(summary = "숙소 요금 조회", description = "특정 숙소의 객실별 요금을 조회합니다")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "숙소를 찾을 수 없음")
    })
    @GetMapping("/{propertyId}/rates")
    public ResponseEntity<ApiResponse<List<RoomRateApiResponse>>> fetchRates(
            @Parameter(description = "숙소 ID") @PathVariable Long propertyId,
            @Valid FetchRateApiRequest request) {

        // 1. Request -> Query 변환 (ApiMapper 사용)
        PropertyRateResult result = fetchRateUseCase.execute(
                RateApiMapper.toQuery(propertyId, request.checkIn(), request.checkOut(), request.guests()));

        // 2. Result -> Response 변환 (ApiMapper 사용)
        List<RoomRateApiResponse> response = result.roomRates().stream()
                .map(RateApiMapper::toApiResponse)
                .toList();

        // 3. 응답 반환
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
