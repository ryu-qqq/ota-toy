package com.ryuqq.otatoy.api.extranet.property.controller;

import com.ryuqq.otatoy.api.core.ApiResponse;
import com.ryuqq.otatoy.api.core.SliceResponse;
import com.ryuqq.otatoy.api.extranet.property.ExtranetPropertyEndpoints;
import com.ryuqq.otatoy.api.extranet.property.dto.response.ExtranetPropertyDetailApiResponse;
import com.ryuqq.otatoy.api.extranet.property.dto.response.ExtranetPropertySummaryApiResponse;
import com.ryuqq.otatoy.api.extranet.property.mapper.PropertyQueryApiMapper;
import com.ryuqq.otatoy.application.property.dto.result.ExtranetPropertySliceResult;
import com.ryuqq.otatoy.application.property.dto.result.PropertyDetail;
import com.ryuqq.otatoy.application.property.port.in.ExtranetSearchPropertyUseCase;
import com.ryuqq.otatoy.application.property.port.in.GetPropertyDetailUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 파트너용 숙소 조회 API Controller.
 * Query(읽기) 전용 — GET 엔드포인트만 포함한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@RestController
@RequestMapping(ExtranetPropertyEndpoints.PROPERTIES)
@Tag(name = "Extranet - 숙소 조회", description = "파트너용 숙소 조회 API")
public class ExtranetPropertyQueryController {

    private final ExtranetSearchPropertyUseCase extranetSearchPropertyUseCase;
    private final GetPropertyDetailUseCase getPropertyDetailUseCase;

    public ExtranetPropertyQueryController(
            ExtranetSearchPropertyUseCase extranetSearchPropertyUseCase,
            GetPropertyDetailUseCase getPropertyDetailUseCase) {
        this.extranetSearchPropertyUseCase = extranetSearchPropertyUseCase;
        this.getPropertyDetailUseCase = getPropertyDetailUseCase;
    }

    @GetMapping
    @Operation(summary = "숙소 목록 조회", description = "파트너가 자기 숙소 목록을 조회합니다")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검증 실패")
    })
    public ResponseEntity<ApiResponse<SliceResponse<ExtranetPropertySummaryApiResponse>>> searchProperties(
            @Parameter(description = "파트너 ID", required = true) @RequestParam Long partnerId,
            @Parameter(description = "페이지 크기 (기본 20)") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "커서 (다음 페이지 조회 시)") @RequestParam(required = false) Long cursor) {

        ExtranetPropertySliceResult result = extranetSearchPropertyUseCase.execute(
            PropertyQueryApiMapper.toExtranetSearchQuery(partnerId, size, cursor));

        List<ExtranetPropertySummaryApiResponse> content = result.content().stream()
                .map(PropertyQueryApiMapper::toSummaryResponse)
                .toList();

        SliceResponse<ExtranetPropertySummaryApiResponse> response =
                SliceResponse.of(content, result.hasNext(), result.nextCursor());

        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @GetMapping(ExtranetPropertyEndpoints.PROPERTY_ID)
    @Operation(summary = "숙소 상세 조회", description = "숙소의 상세 정보를 조회합니다 (사진/편의시설/속성값/객실 포함)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "숙소를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<ExtranetPropertyDetailApiResponse>> getPropertyDetail(
            @PathVariable(ExtranetPropertyEndpoints.PATH_PROPERTY_ID) Long propertyId) {

        PropertyDetail result = getPropertyDetailUseCase.execute(
            PropertyQueryApiMapper.toPropertyId(propertyId));

        ExtranetPropertyDetailApiResponse response = PropertyQueryApiMapper.toDetailResponse(result);
        return ResponseEntity.ok(ApiResponse.of(response));
    }
}
