package com.ryuqq.otatoy.api.extranet.property;

import com.ryuqq.otatoy.api.core.ApiResponse;
import com.ryuqq.otatoy.api.extranet.property.dto.SetPropertyPhotosApiRequest;
import com.ryuqq.otatoy.api.extranet.property.dto.RegisterPropertyApiRequest;
import com.ryuqq.otatoy.api.extranet.property.dto.SetPropertyAmenitiesApiRequest;
import com.ryuqq.otatoy.api.extranet.property.dto.SetPropertyAttributesApiRequest;
import com.ryuqq.otatoy.api.extranet.property.mapper.PropertyAmenityApiMapper;
import com.ryuqq.otatoy.api.extranet.property.mapper.PropertyApiMapper;
import com.ryuqq.otatoy.api.extranet.property.mapper.PropertyAttributeApiMapper;
import com.ryuqq.otatoy.api.extranet.property.mapper.PropertyPhotoApiMapper;
import com.ryuqq.otatoy.api.extranet.property.mapper.PropertyQueryApiMapper;
import com.ryuqq.otatoy.application.property.dto.result.ExtranetPropertySliceResult;
import com.ryuqq.otatoy.application.property.dto.result.PropertyDetail;
import com.ryuqq.otatoy.application.property.port.in.ExtranetSearchPropertyUseCase;
import com.ryuqq.otatoy.application.property.port.in.GetPropertyDetailUseCase;
import com.ryuqq.otatoy.application.property.port.in.SetPropertyPhotosUseCase;
import com.ryuqq.otatoy.application.property.port.in.RegisterPropertyUseCase;
import com.ryuqq.otatoy.application.property.port.in.SetPropertyAmenitiesUseCase;
import com.ryuqq.otatoy.application.property.port.in.SetPropertyAttributesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 파트너용 숙소 관리 API Controller.
 * UseCase 인터페이스만 의존하며, 비즈니스 로직을 포함하지 않는다 (AC-3, AC-8, AC-10).
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@RestController
@RequestMapping(ExtranetPropertyEndpoints.PROPERTIES)
@Tag(name = "Extranet - 숙소 관리", description = "파트너용 숙소 등록/관리 API")
public class ExtranetPropertyController {

    private final RegisterPropertyUseCase registerPropertyUseCase;
    private final SetPropertyPhotosUseCase setPropertyPhotosUseCase;
    private final SetPropertyAmenitiesUseCase setPropertyAmenitiesUseCase;
    private final SetPropertyAttributesUseCase setPropertyAttributesUseCase;
    private final ExtranetSearchPropertyUseCase extranetSearchPropertyUseCase;
    private final GetPropertyDetailUseCase getPropertyDetailUseCase;

    public ExtranetPropertyController(
            RegisterPropertyUseCase registerPropertyUseCase,
            SetPropertyPhotosUseCase setPropertyPhotosUseCase,
            SetPropertyAmenitiesUseCase setPropertyAmenitiesUseCase,
            SetPropertyAttributesUseCase setPropertyAttributesUseCase,
            ExtranetSearchPropertyUseCase extranetSearchPropertyUseCase,
            GetPropertyDetailUseCase getPropertyDetailUseCase) {
        this.registerPropertyUseCase = registerPropertyUseCase;
        this.setPropertyPhotosUseCase = setPropertyPhotosUseCase;
        this.setPropertyAmenitiesUseCase = setPropertyAmenitiesUseCase;
        this.setPropertyAttributesUseCase = setPropertyAttributesUseCase;
        this.extranetSearchPropertyUseCase = extranetSearchPropertyUseCase;
        this.getPropertyDetailUseCase = getPropertyDetailUseCase;
    }

    /**
     * 파트너의 숙소 목록을 조회한다.
     * partnerId 기반 커서 페이징을 지원한다.
     */
    @GetMapping
    @Operation(summary = "숙소 목록 조회", description = "파트너가 자기 숙소 목록을 조회합니다")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "검증 실패")
    })
    public ResponseEntity<ApiResponse<ExtranetPropertySliceResult>> searchProperties(
            @Parameter(description = "파트너 ID", required = true)
            @RequestParam Long partnerId,
            @Parameter(description = "페이지 크기 (기본 20)")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "커서 (다음 페이지 조회 시)")
            @RequestParam(required = false) Long cursor) {

        ExtranetPropertySliceResult result = extranetSearchPropertyUseCase.execute(
            PropertyQueryApiMapper.toExtranetSearchQuery(partnerId, size, cursor));

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 숙소 상세정보를 조회한다.
     * 사진, 편의시설, 속성값, 객실 목록을 포함한다.
     */
    @GetMapping(ExtranetPropertyEndpoints.PROPERTY_ID)
    @Operation(summary = "숙소 상세 조회", description = "숙소의 상세 정보를 조회합니다 (사진/편의시설/속성값/객실 포함)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "숙소를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<PropertyDetail>> getPropertyDetail(
            @PathVariable(ExtranetPropertyEndpoints.PATH_PROPERTY_ID) Long propertyId) {

        PropertyDetail result = getPropertyDetailUseCase.execute(
            PropertyQueryApiMapper.toPropertyId(propertyId));

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 숙소 기본정보를 등록한다.
     * 편의시설/사진/속성값은 별도 API로 분리되어 있다.
     */
    @PostMapping
    @Operation(summary = "숙소 기본정보 등록", description = "파트너가 새로운 숙소를 등록합니다")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", description = "등록 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "검증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "파트너를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Long>> registerProperty(
            @Valid @RequestBody RegisterPropertyApiRequest request) {

        Long propertyId = registerPropertyUseCase.execute(
            PropertyApiMapper.toCommand(request));

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(propertyId));
    }

    /**
     * 숙소 사진을 설정한다 (전체 교체 방식, diff 기반).
     */
    @PutMapping(ExtranetPropertyEndpoints.PROPERTY_ID + "/photos")
    @Operation(summary = "숙소 사진 설정", description = "숙소의 사진을 전체 교체합니다 (diff 기반)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "설정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "검증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "숙소를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Void>> setPhotos(
            @PathVariable(ExtranetPropertyEndpoints.PATH_PROPERTY_ID) Long propertyId,
            @Valid @RequestBody SetPropertyPhotosApiRequest request) {

        setPropertyPhotosUseCase.execute(
            PropertyPhotoApiMapper.toCommand(propertyId, request));

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 숙소 편의시설을 설정한다 (전체 교체 방식).
     */
    @PutMapping(ExtranetPropertyEndpoints.PROPERTY_ID + "/amenities")
    @Operation(summary = "숙소 편의시설 설정", description = "숙소의 편의시설을 전체 교체합니다")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "설정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "검증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "숙소를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Void>> setAmenities(
            @PathVariable(ExtranetPropertyEndpoints.PATH_PROPERTY_ID) Long propertyId,
            @Valid @RequestBody SetPropertyAmenitiesApiRequest request) {

        setPropertyAmenitiesUseCase.execute(
            PropertyAmenityApiMapper.toCommand(propertyId, request));

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 숙소 속성값을 설정한다 (전체 교체 방식).
     * 필수 속성 누락 시 UseCase에서 DomainException(ACC-006)이 발생하여 400으로 응답한다.
     */
    @PutMapping(ExtranetPropertyEndpoints.PROPERTY_ID + "/attributes")
    @Operation(summary = "숙소 속성값 설정", description = "숙소의 속성값을 전체 교체합니다")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "설정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "검증 실패 또는 필수 속성 누락"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "숙소를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Void>> setAttributes(
            @PathVariable(ExtranetPropertyEndpoints.PATH_PROPERTY_ID) Long propertyId,
            @Valid @RequestBody SetPropertyAttributesApiRequest request) {

        setPropertyAttributesUseCase.execute(
            PropertyAttributeApiMapper.toCommand(propertyId, request));

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
