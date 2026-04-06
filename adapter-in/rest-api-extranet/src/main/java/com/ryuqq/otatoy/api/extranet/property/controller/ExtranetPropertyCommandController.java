package com.ryuqq.otatoy.api.extranet.property.controller;

import com.ryuqq.otatoy.api.core.ApiResponse;
import com.ryuqq.otatoy.api.extranet.property.ExtranetPropertyEndpoints;
import com.ryuqq.otatoy.api.extranet.property.dto.request.RegisterPropertyApiRequest;
import com.ryuqq.otatoy.api.extranet.property.dto.request.SetPropertyAmenitiesApiRequest;
import com.ryuqq.otatoy.api.extranet.property.dto.request.SetPropertyAttributesApiRequest;
import com.ryuqq.otatoy.api.extranet.property.dto.request.SetPropertyPhotosApiRequest;
import com.ryuqq.otatoy.api.extranet.property.mapper.PropertyAmenityApiMapper;
import com.ryuqq.otatoy.api.extranet.property.mapper.PropertyApiMapper;
import com.ryuqq.otatoy.api.extranet.property.mapper.PropertyAttributeApiMapper;
import com.ryuqq.otatoy.api.extranet.property.mapper.PropertyPhotoApiMapper;
import com.ryuqq.otatoy.application.property.port.in.RegisterPropertyUseCase;
import com.ryuqq.otatoy.application.property.port.in.SetPropertyAmenitiesUseCase;
import com.ryuqq.otatoy.application.property.port.in.SetPropertyAttributesUseCase;
import com.ryuqq.otatoy.application.property.port.in.SetPropertyPhotosUseCase;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 파트너용 숙소 등록/설정 API Controller.
 * Command(쓰기) 전용 — POST/PUT 엔드포인트만 포함한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@RestController
@RequestMapping(ExtranetPropertyEndpoints.PROPERTIES)
@Tag(name = "Extranet - 숙소 등록/설정", description = "파트너용 숙소 등록 및 부속 정보 설정 API")
public class ExtranetPropertyCommandController {

    private final RegisterPropertyUseCase registerPropertyUseCase;
    private final SetPropertyPhotosUseCase setPropertyPhotosUseCase;
    private final SetPropertyAmenitiesUseCase setPropertyAmenitiesUseCase;
    private final SetPropertyAttributesUseCase setPropertyAttributesUseCase;

    public ExtranetPropertyCommandController(
            RegisterPropertyUseCase registerPropertyUseCase,
            SetPropertyPhotosUseCase setPropertyPhotosUseCase,
            SetPropertyAmenitiesUseCase setPropertyAmenitiesUseCase,
            SetPropertyAttributesUseCase setPropertyAttributesUseCase) {
        this.registerPropertyUseCase = registerPropertyUseCase;
        this.setPropertyPhotosUseCase = setPropertyPhotosUseCase;
        this.setPropertyAmenitiesUseCase = setPropertyAmenitiesUseCase;
        this.setPropertyAttributesUseCase = setPropertyAttributesUseCase;
    }

    @PostMapping
    @Operation(summary = "숙소 기본정보 등록", description = "파트너가 새로운 숙소를 등록합니다")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "등록 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "파트너를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Long>> registerProperty(
            @Valid @RequestBody RegisterPropertyApiRequest request) {

        Long propertyId = registerPropertyUseCase.execute(PropertyApiMapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(propertyId));
    }

    @PutMapping(ExtranetPropertyEndpoints.REL_PHOTOS)
    @Operation(summary = "숙소 사진 설정", description = "숙소의 사진을 전체 교체합니다 (diff 기반)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "설정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "숙소를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Void>> setPhotos(
            @PathVariable(ExtranetPropertyEndpoints.PATH_PROPERTY_ID) Long propertyId,
            @Valid @RequestBody SetPropertyPhotosApiRequest request) {

        setPropertyPhotosUseCase.execute(PropertyPhotoApiMapper.toCommand(propertyId, request));
        return ResponseEntity.ok(ApiResponse.of());
    }

    @PutMapping(ExtranetPropertyEndpoints.REL_AMENITIES)
    @Operation(summary = "숙소 편의시설 설정", description = "숙소의 편의시설을 전체 교체합니다")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "설정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "숙소를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Void>> setAmenities(
            @PathVariable(ExtranetPropertyEndpoints.PATH_PROPERTY_ID) Long propertyId,
            @Valid @RequestBody SetPropertyAmenitiesApiRequest request) {

        setPropertyAmenitiesUseCase.execute(PropertyAmenityApiMapper.toCommand(propertyId, request));
        return ResponseEntity.ok(ApiResponse.of());
    }

    @PutMapping(ExtranetPropertyEndpoints.REL_ATTRIBUTES)
    @Operation(summary = "숙소 속성값 설정", description = "숙소의 속성값을 전체 교체합니다")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "설정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "숙소를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Void>> setAttributes(
            @PathVariable(ExtranetPropertyEndpoints.PATH_PROPERTY_ID) Long propertyId,
            @Valid @RequestBody SetPropertyAttributesApiRequest request) {

        setPropertyAttributesUseCase.execute(PropertyAttributeApiMapper.toCommand(propertyId, request));
        return ResponseEntity.ok(ApiResponse.of());
    }
}
