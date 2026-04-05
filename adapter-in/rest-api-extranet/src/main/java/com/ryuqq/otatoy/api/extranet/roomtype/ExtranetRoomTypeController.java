package com.ryuqq.otatoy.api.extranet.roomtype;

import com.ryuqq.otatoy.api.core.ApiResponse;
import com.ryuqq.otatoy.api.extranet.roomtype.dto.RegisterRoomTypeApiRequest;
import com.ryuqq.otatoy.api.extranet.roomtype.mapper.RoomTypeApiMapper;
import com.ryuqq.otatoy.application.roomtype.port.in.RegisterRoomTypeUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.ryuqq.otatoy.api.extranet.property.ExtranetPropertyEndpoints;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 파트너용 객실 유형 관리 API Controller.
 * UseCase 인터페이스만 의존하며, 비즈니스 로직을 포함하지 않는다 (AC-10).
 * <p>
 * baseOccupancy > maxOccupancy 검증은 Domain 레이어에서 수행한다 (AC-6).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@RestController
@RequestMapping(ExtranetPropertyEndpoints.ROOMS)
@Tag(name = "Extranet - 객실 관리", description = "파트너용 객실 유형 등록/관리 API")
public class ExtranetRoomTypeController {

    private final RegisterRoomTypeUseCase registerRoomTypeUseCase;

    public ExtranetRoomTypeController(RegisterRoomTypeUseCase registerRoomTypeUseCase) {
        this.registerRoomTypeUseCase = registerRoomTypeUseCase;
    }

    /**
     * 숙소에 객실 유형을 등록한다.
     */
    @PostMapping
    @Operation(summary = "객실 유형 등록", description = "숙소에 새로운 객실 유형을 등록합니다")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", description = "등록 성공 — roomTypeId 반환"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "검증 실패 (baseOccupancy > maxOccupancy 등)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "숙소를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Long>> registerRoomType(
            @PathVariable(ExtranetPropertyEndpoints.PATH_PROPERTY_ID) Long propertyId,
            @Valid @RequestBody RegisterRoomTypeApiRequest request) {

        Long roomTypeId = registerRoomTypeUseCase.execute(
            RoomTypeApiMapper.toCommand(propertyId, request));

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(roomTypeId));
    }
}
