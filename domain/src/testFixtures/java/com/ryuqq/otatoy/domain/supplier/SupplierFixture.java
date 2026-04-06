package com.ryuqq.otatoy.domain.supplier;

import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.domain.common.vo.Email;
import com.ryuqq.otatoy.domain.common.vo.PhoneNumber;

import java.time.Instant;

/**
 * Supplier BC 테스트용 Fixture.
 * 다양한 상태의 Supplier, SupplierProperty, SupplierRoomType, SupplierSyncLog를 생성한다.
 */
public final class SupplierFixture {

    private SupplierFixture() {}

    // === 기본 상수 ===
    public static final Instant DEFAULT_NOW = Instant.parse("2026-04-04T00:00:00Z");
    public static final SupplierName DEFAULT_NAME = SupplierName.of("TestSupplier");
    public static final SupplierNameKr DEFAULT_NAME_KR = SupplierNameKr.of("테스트공급자");
    public static final CompanyTitle DEFAULT_COMPANY_TITLE = CompanyTitle.of("테스트컴퍼니");
    public static final OwnerName DEFAULT_OWNER_NAME = OwnerName.of("홍길동");
    public static final BusinessNo DEFAULT_BUSINESS_NO = BusinessNo.of("123-45-67890");
    public static final String DEFAULT_ADDRESS = "서울시 강남구 테헤란로 123";
    public static final PhoneNumber DEFAULT_PHONE = PhoneNumber.of("02-1234-5678");
    public static final Email DEFAULT_EMAIL = Email.of("supplier@test.com");
    public static final String DEFAULT_TERMS_URL = "https://test.com/terms";

    // === Supplier Fixture ===

    /**
     * 신규 ACTIVE 상태 공급자
     */
    public static Supplier activeSupplier() {
        return Supplier.forNew(
                DEFAULT_NAME, DEFAULT_NAME_KR, DEFAULT_COMPANY_TITLE,
                DEFAULT_OWNER_NAME, DEFAULT_BUSINESS_NO, DEFAULT_ADDRESS,
                DEFAULT_PHONE, DEFAULT_EMAIL, DEFAULT_TERMS_URL, DEFAULT_NOW
        );
    }

    /**
     * 지정 상태의 공급자 (reconstitute 사용)
     */
    public static Supplier supplierWithStatus(SupplierStatus status) {
        return Supplier.reconstitute(
                SupplierId.of(1L), DEFAULT_NAME, DEFAULT_NAME_KR, DEFAULT_COMPANY_TITLE,
                DEFAULT_OWNER_NAME, DEFAULT_BUSINESS_NO, DEFAULT_ADDRESS,
                DEFAULT_PHONE, DEFAULT_EMAIL, DEFAULT_TERMS_URL,
                status, DEFAULT_NOW, DEFAULT_NOW
        );
    }

    /**
     * SUSPENDED 상태 공급자
     */
    public static Supplier suspendedSupplier() {
        return supplierWithStatus(SupplierStatus.SUSPENDED);
    }

    /**
     * TERMINATED 상태 공급자
     */
    public static Supplier terminatedSupplier() {
        return supplierWithStatus(SupplierStatus.TERMINATED);
    }

    /**
     * DB 복원된 ACTIVE 공급자
     */
    public static Supplier reconstitutedSupplier() {
        return supplierWithStatus(SupplierStatus.ACTIVE);
    }

    // === SupplierProperty Fixture ===

    /**
     * 신규 MAPPED 상태 공급자 숙소 매핑
     */
    public static SupplierProperty mappedProperty() {
        return SupplierProperty.forNew(
                SupplierId.of(1L), PropertyId.of(100L), "EXT-PROP-001", DEFAULT_NOW
        );
    }

    /**
     * DB 복원된 공급자 숙소 매핑
     */
    public static SupplierProperty reconstitutedProperty(SupplierMappingStatus status) {
        return SupplierProperty.reconstitute(
                SupplierPropertyId.of(1L), SupplierId.of(1L), PropertyId.of(100L),
                "EXT-PROP-001", DEFAULT_NOW, status, DEFAULT_NOW, DEFAULT_NOW
        );
    }

    /**
     * UNMAPPED 상태 공급자 숙소
     */
    public static SupplierProperty unmappedProperty() {
        return reconstitutedProperty(SupplierMappingStatus.UNMAPPED);
    }

    // === SupplierRoomType Fixture ===

    /**
     * 신규 MAPPED 상태 공급자 객실 매핑
     */
    public static SupplierRoomType mappedRoomType() {
        return SupplierRoomType.forNew(
                SupplierPropertyId.of(1L), RoomTypeId.of(200L), "EXT-ROOM-001", DEFAULT_NOW
        );
    }

    /**
     * DB 복원된 공급자 객실 매핑
     */
    public static SupplierRoomType reconstitutedRoomType(SupplierMappingStatus status) {
        return SupplierRoomType.reconstitute(
                SupplierRoomTypeId.of(1L), SupplierPropertyId.of(1L), RoomTypeId.of(200L),
                "EXT-ROOM-001", DEFAULT_NOW, status, DEFAULT_NOW, DEFAULT_NOW
        );
    }

    /**
     * UNMAPPED 상태 공급자 객실
     */
    public static SupplierRoomType unmappedRoomType() {
        return reconstitutedRoomType(SupplierMappingStatus.UNMAPPED);
    }

    // === SupplierSyncLog Fixture ===

    /**
     * 성공 동기화 로그
     */
    public static SupplierSyncLog successSyncLog() {
        return SupplierSyncLog.forSuccess(
                SupplierId.of(1L), SupplierSyncType.PROPERTY, DEFAULT_NOW,
                10, 5, 3, 2
        );
    }

    /**
     * 실패 동기화 로그
     */
    public static SupplierSyncLog failedSyncLog() {
        return SupplierSyncLog.forFailed(
                SupplierId.of(1L), SupplierSyncType.PROPERTY, DEFAULT_NOW,
                "Connection timeout"
        );
    }

    /**
     * DB 복원된 동기화 로그
     */
    public static SupplierSyncLog reconstitutedSyncLog(SupplierSyncStatus status) {
        return SupplierSyncLog.reconstitute(
                SupplierSyncLogId.of(1L), SupplierId.of(1L), SupplierSyncType.FULL, DEFAULT_NOW,
                status, 10, 5, 3, 2, status == SupplierSyncStatus.FAILED ? "에러 발생" : null,
                DEFAULT_NOW, DEFAULT_NOW
        );
    }

    // === SupplierApiConfig Fixture ===

    /**
     * 기본 SupplierApiConfig (supplierId=1, 30분 간격)
     */
    public static SupplierApiConfig activeApiConfig() {
        return SupplierApiConfig.reconstitute(
                1L, SupplierId.of(1L), SupplierApiType.MOCK,
                "https://api.test.com", "test-api-key", "BEARER",
                30, DEFAULT_NOW, DEFAULT_NOW
        );
    }

    /**
     * 지정 supplierId의 SupplierApiConfig
     */
    public static SupplierApiConfig apiConfigForSupplier(Long supplierId) {
        return SupplierApiConfig.reconstitute(
                supplierId, SupplierId.of(supplierId), SupplierApiType.MOCK,
                "https://api.test.com", "test-api-key", "BEARER",
                30, DEFAULT_NOW, DEFAULT_NOW
        );
    }

    // === SupplierTask Fixture ===

    /**
     * PENDING 상태의 PROPERTY_CONTENT Task
     */
    public static SupplierTask pendingPropertyContentTask() {
        return SupplierTask.forNew(
                SupplierId.of(1L), 1L,
                SupplierTaskType.PROPERTY_CONTENT, null, 3, DEFAULT_NOW
        );
    }

    /**
     * PENDING 상태의 RATE_AVAILABILITY Task
     */
    public static SupplierTask pendingRateAvailabilityTask() {
        return SupplierTask.forNew(
                SupplierId.of(1L), 1L,
                SupplierTaskType.RATE_AVAILABILITY, null, 3, DEFAULT_NOW
        );
    }

    /**
     * COMPLETED 상태의 PROPERTY_CONTENT Task (DB 복원)
     */
    public static SupplierTask completedPropertyContentTask() {
        return SupplierTask.reconstitute(
                SupplierTaskId.of(1L), SupplierId.of(1L), 1L,
                SupplierTaskType.PROPERTY_CONTENT, SupplierTaskStatus.COMPLETED, null,
                0, 3, null, DEFAULT_NOW, DEFAULT_NOW
        );
    }

    /**
     * COMPLETED 상태의 RATE_AVAILABILITY Task (DB 복원)
     */
    public static SupplierTask completedRateAvailabilityTask() {
        return SupplierTask.reconstitute(
                SupplierTaskId.of(2L), SupplierId.of(1L), 1L,
                SupplierTaskType.RATE_AVAILABILITY, SupplierTaskStatus.COMPLETED, null,
                0, 3, null, DEFAULT_NOW, DEFAULT_NOW
        );
    }

    /**
     * FAILED 상태의 Task (재시도 가능, DB 복원)
     */
    public static SupplierTask failedRetryableTask() {
        return SupplierTask.reconstitute(
                SupplierTaskId.of(3L), SupplierId.of(1L), 1L,
                SupplierTaskType.PROPERTY_CONTENT, SupplierTaskStatus.FAILED, null,
                1, 3, "Connection timeout", DEFAULT_NOW, DEFAULT_NOW
        );
    }

    // === SupplierRawData Fixture ===

    /**
     * FETCHED 상태의 RawData
     */
    public static SupplierRawData fetchedRawData() {
        return SupplierRawData.forNew(
                SupplierId.of(1L), SupplierTaskType.PROPERTY_CONTENT, SupplierApiType.MOCK, "{\"properties\":[]}", DEFAULT_NOW
        );
    }
}
