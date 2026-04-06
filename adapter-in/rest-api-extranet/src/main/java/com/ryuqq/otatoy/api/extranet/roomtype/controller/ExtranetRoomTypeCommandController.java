package com.ryuqq.otatoy.api.extranet.roomtype.controller;

import com.ryuqq.otatoy.api.core.ApiResponse;
import com.ryuqq.otatoy.api.extranet.roomtype.ExtranetRoomTypeEndpoints;
import com.ryuqq.otatoy.api.extranet.roomtype.dto.request.RegisterRoomTypeApiRequest;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 파트너용 객실 유형 등록 API Controller.
 * Command(쓰기) 전용 — POST 엔드포인트만 포함한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@RestController
@RequestMapping(ExtranetRoomTypeEndpoints.ROOMS)
@Tag(name = "Extranet - 객실 등록", description = "파트너용 객실 유형 등록 API")
public class ExtranetRoomTypeCommandController {

    private final RegisterRoomTypeUseCase registerRoomTypeUseCase;

    public ExtranetRoomTypeCommandController(RegisterRoomTypeUseCase registerRoomTypeUseCase) {
        this.registerRoomTypeUseCase = registerRoomTypeUseCase;
    }

    @PostMapping
    @Operation(summary = "객실 유형 등록", description = "숙소에 새로운 객실 유형을 등록합니다")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "등록 성공 — roomTypeId 반환"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "숙소를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Long>> registerRoomType(
            @PathVariable(ExtranetRoomTypeEndpoints.PATH_PROPERTY_ID) Long propertyId,
            @Valid @RequestBody RegisterRoomTypeApiRequest request) {

        Long roomTypeId = registerRoomTypeUseCase.execute(RoomTypeApiMapper.toCommand(propertyId, request));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(roomTypeId));
    }
}
