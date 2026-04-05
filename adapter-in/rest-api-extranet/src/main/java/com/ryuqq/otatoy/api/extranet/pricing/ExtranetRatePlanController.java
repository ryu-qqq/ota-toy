package com.ryuqq.otatoy.api.extranet.pricing;

import com.ryuqq.otatoy.api.core.ApiResponse;
import com.ryuqq.otatoy.api.extranet.pricing.dto.RegisterRatePlanApiRequest;
import com.ryuqq.otatoy.api.extranet.pricing.dto.SetRateAndInventoryApiRequest;
import com.ryuqq.otatoy.api.extranet.pricing.mapper.RateAndInventoryApiMapper;
import com.ryuqq.otatoy.api.extranet.pricing.mapper.RatePlanApiMapper;
import com.ryuqq.otatoy.api.extranet.property.ExtranetPropertyEndpoints;
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
 * 파트너용 요금 정책(RatePlan) 관리 API Controller.
 * UseCase 인터페이스만 의존하며, 비즈니스 로직을 포함하지 않는다 (AC-1, AC-10).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@RestController
@Tag(name = "Extranet - 요금 정책 관리", description = "파트너용 요금 정책(RatePlan) 등록/관리 API")
public class ExtranetRatePlanController {

    private final RegisterRatePlanUseCase registerRatePlanUseCase;
    private final SetRateAndInventoryUseCase setRateAndInventoryUseCase;

    public ExtranetRatePlanController(
            RegisterRatePlanUseCase registerRatePlanUseCase,
            SetRateAndInventoryUseCase setRateAndInventoryUseCase) {
        this.registerRatePlanUseCase = registerRatePlanUseCase;
        this.setRateAndInventoryUseCase = setRateAndInventoryUseCase;
    }

    /**
     * 객실 유형에 요금 정책을 등록한다.
     */
    @PostMapping(ExtranetRatePlanEndpoints.BASE)
    @Operation(summary = "요금 정책 등록", description = "객실 유형에 새로운 요금 정책(RatePlan)을 등록합니다")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", description = "등록 성공 — ratePlanId 반환"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "검증 실패 (무료 취소 + 환불 불가 동시 설정 등)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "객실 유형을 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Long>> registerRatePlan(
            @PathVariable(ExtranetPropertyEndpoints.PATH_PROPERTY_ID) Long propertyId,
            @PathVariable(ExtranetRatePlanEndpoints.PATH_ROOM_TYPE_ID) Long roomTypeId,
            @Valid @RequestBody RegisterRatePlanApiRequest request) {

        Long ratePlanId = registerRatePlanUseCase.execute(
            RatePlanApiMapper.toCommand(roomTypeId, request));

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(ratePlanId));
    }

    /**
     * 요금 정책에 대한 요금/재고를 일괄 설정한다.
     * RateRule(요일별 요금), RateOverride(날짜별 오버라이드), Rate 스냅샷, Inventory를 한 번에 생성한다.
     */
    @PutMapping(ExtranetRatePlanEndpoints.RATE_PLAN_RATES)
    @Operation(summary = "요금/재고 설정", description = "요금 정책에 대한 요일별 요금, 날짜별 오버라이드, 재고를 일괄 설정합니다")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "설정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "검증 실패 (날짜 범위 오류, 요금 음수 등)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "요금 정책을 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Void>> setRateAndInventory(
            @PathVariable(ExtranetRatePlanEndpoints.PATH_RATE_PLAN_ID) Long ratePlanId,
            @Valid @RequestBody SetRateAndInventoryApiRequest request) {

        setRateAndInventoryUseCase.execute(
            RateAndInventoryApiMapper.toCommand(ratePlanId, request));

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
