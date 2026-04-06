package com.ryuqq.otatoy.api.extranet.pricing.controller;

import com.ryuqq.otatoy.api.core.ApiResponse;
import com.ryuqq.otatoy.api.extranet.pricing.ExtranetRatePlanEndpoints;
import com.ryuqq.otatoy.api.extranet.pricing.dto.request.RegisterRatePlanApiRequest;
import com.ryuqq.otatoy.api.extranet.pricing.dto.request.SetRateAndInventoryApiRequest;
import com.ryuqq.otatoy.api.extranet.pricing.mapper.RateAndInventoryApiMapper;
import com.ryuqq.otatoy.api.extranet.pricing.mapper.RatePlanApiMapper;
import com.ryuqq.otatoy.application.pricing.port.in.RegisterRatePlanUseCase;
import com.ryuqq.otatoy.application.pricing.port.in.SetRateAndInventoryUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 파트너용 요금 정책 등록/설정 API Controller.
 * Command(쓰기) 전용 — POST/PUT 엔드포인트만 포함한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@RestController
@Tag(name = "Extranet - 요금 정책 관리", description = "파트너용 요금 정책(RatePlan) 등록/관리 API")
public class ExtranetRatePlanCommandController {

    private final RegisterRatePlanUseCase registerRatePlanUseCase;
    private final SetRateAndInventoryUseCase setRateAndInventoryUseCase;

    public ExtranetRatePlanCommandController(
            RegisterRatePlanUseCase registerRatePlanUseCase,
            SetRateAndInventoryUseCase setRateAndInventoryUseCase) {
        this.registerRatePlanUseCase = registerRatePlanUseCase;
        this.setRateAndInventoryUseCase = setRateAndInventoryUseCase;
    }

    @PostMapping(ExtranetRatePlanEndpoints.RATE_PLANS)
    @Operation(summary = "요금 정책 등록", description = "객실 유형에 새로운 요금 정책(RatePlan)을 등록합니다")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "등록 성공 — ratePlanId 반환"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "객실 유형을 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Long>> registerRatePlan(
            @PathVariable(ExtranetRatePlanEndpoints.PATH_PROPERTY_ID) Long propertyId,
            @PathVariable(ExtranetRatePlanEndpoints.PATH_ROOM_TYPE_ID) Long roomTypeId,
            @Valid @RequestBody RegisterRatePlanApiRequest request) {

        Long ratePlanId = registerRatePlanUseCase.execute(RatePlanApiMapper.toCommand(roomTypeId, request));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(ratePlanId));
    }

    @PutMapping(ExtranetRatePlanEndpoints.RATE_PLAN_RATES)
    @Operation(summary = "요금/재고 설정", description = "요금 정책에 대한 요일별 요금, 날짜별 오버라이드, 재고를 일괄 설정합니다")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "설정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "요금 정책을 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Void>> setRateAndInventory(
            @PathVariable(ExtranetRatePlanEndpoints.PATH_RATE_PLAN_ID) Long ratePlanId,
            @Valid @RequestBody SetRateAndInventoryApiRequest request) {

        setRateAndInventoryUseCase.execute(RateAndInventoryApiMapper.toCommand(ratePlanId, request));
        return ResponseEntity.ok(ApiResponse.of());
    }
}
