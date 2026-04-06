package com.ryuqq.otatoy.api.extranet.common.error;

import com.ryuqq.otatoy.api.core.ErrorMapper.MappedError;
import com.ryuqq.otatoy.domain.accommodation.AccommodationErrorCode;
import com.ryuqq.otatoy.domain.common.DomainException;
import com.ryuqq.otatoy.domain.common.ErrorCategory;
import com.ryuqq.otatoy.domain.common.ErrorCode;
import com.ryuqq.otatoy.domain.inventory.InventoryErrorCode;
import com.ryuqq.otatoy.domain.partner.PartnerErrorCode;
import com.ryuqq.otatoy.domain.pricing.PricingErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ExtranetPropertyErrorMapper 단위 테스트.
 * DomainException -> HTTP 상태/메시지 변환 정확성을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@DisplayName("ExtranetPropertyErrorMapper")
class ExtranetPropertyErrorMapperTest {

    private final ExtranetPropertyErrorMapper mapper = new ExtranetPropertyErrorMapper();

    // =========================================================================
    // supports() 테스트
    // =========================================================================

    @Nested
    @DisplayName("supports")
    class Supports {

        @ParameterizedTest
        @ValueSource(strings = {"ACC-001", "ACC-002", "ACC-004", "ACC-005", "ACC-006"})
        @DisplayName("ACC- 접두사 에러 코드를 지원한다")
        void shouldSupportAccPrefix(String code) {
            DomainException ex = createException(code, "메시지", ErrorCategory.NOT_FOUND);

            assertThat(mapper.supports(ex)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"PTN-001", "PTN-002", "PTN-003", "PTN-004"})
        @DisplayName("PTN- 접두사 에러 코드를 지원한다")
        void shouldSupportPtnPrefix(String code) {
            DomainException ex = createException(code, "메시지", ErrorCategory.NOT_FOUND);

            assertThat(mapper.supports(ex)).isTrue();
        }

        @Test
        @DisplayName("PT- 접두사 에러 코드를 지원한다")
        void shouldSupportPtPrefix() {
            DomainException ex = createException("PT-001", "메시지", ErrorCategory.NOT_FOUND);

            assertThat(mapper.supports(ex)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"PRC-001", "PRC-002", "PRC-003", "PRC-004"})
        @DisplayName("PRC- 접두사 에러 코드를 지원한다")
        void shouldSupportPrcPrefix(String code) {
            DomainException ex = createException(code, "메시지", ErrorCategory.NOT_FOUND);

            assertThat(mapper.supports(ex)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"INV-001", "INV-002", "INV-003", "INV-004"})
        @DisplayName("INV- 접두사 에러 코드를 지원한다")
        void shouldSupportInvPrefix(String code) {
            DomainException ex = createException(code, "메시지", ErrorCategory.NOT_FOUND);

            assertThat(mapper.supports(ex)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"RT-001", "RT-002"})
        @DisplayName("RT- 접두사 에러 코드를 지원한다")
        void shouldSupportRtPrefix(String code) {
            DomainException ex = createException(code, "메시지", ErrorCategory.NOT_FOUND);

            assertThat(mapper.supports(ex)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"USR-001", "ORD-001", "UNKNOWN-001", "SPL-001"})
        @DisplayName("지원하지 않는 접두사 에러 코드는 false를 반환한다")
        void shouldNotSupportUnsupportedPrefix(String code) {
            DomainException ex = createException(code, "메시지", ErrorCategory.NOT_FOUND);

            assertThat(mapper.supports(ex)).isFalse();
        }
    }

    // =========================================================================
    // map() 테스트 - ErrorCategory -> HTTP 상태 매핑
    // =========================================================================

    @Nested
    @DisplayName("map - ErrorCategory별 HTTP 상태 매핑")
    class MapByCategory {

        @Test
        @DisplayName("NOT_FOUND 카테고리는 404 Not Found로 매핑된다")
        void shouldMapNotFoundTo404() {
            DomainException ex = createException("ACC-001", "숙소를 찾을 수 없습니다", ErrorCategory.NOT_FOUND);

            MappedError result = mapper.map(ex);

            assertThat(result.status()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(result.code()).isEqualTo("ACC-001");
            assertThat(result.title()).isEqualTo("숙소를 찾을 수 없습니다");
            assertThat(result.detail()).isEqualTo("숙소를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("VALIDATION 카테고리는 400 Bad Request로 매핑된다")
        void shouldMapValidationTo400() {
            DomainException ex = createException("ACC-006", "필수 속성이 누락되었습니다", ErrorCategory.VALIDATION);

            MappedError result = mapper.map(ex);

            assertThat(result.status()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("CONFLICT 카테고리는 409 Conflict로 매핑된다")
        void shouldMapConflictTo409() {
            DomainException ex = createException("INV-002", "재고가 소진되었습니다", ErrorCategory.CONFLICT);

            MappedError result = mapper.map(ex);

            assertThat(result.status()).isEqualTo(HttpStatus.CONFLICT);
        }

        @Test
        @DisplayName("FORBIDDEN 카테고리는 422 Unprocessable Entity로 매핑된다")
        void shouldMapForbiddenTo422() {
            DomainException ex = createException("INV-003", "판매가 중지된 재고입니다", ErrorCategory.FORBIDDEN);

            MappedError result = mapper.map(ex);

            assertThat(result.status()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    // =========================================================================
    // map() 테스트 - 실제 도메인 예외
    // =========================================================================

    @Nested
    @DisplayName("map - 실제 도메인 예외 사용")
    class MapWithRealExceptions {

        @Test
        @DisplayName("PropertyNotFoundException이 404로 매핑된다")
        void shouldMapPropertyNotFoundTo404() {
            DomainException ex = createExceptionFrom(AccommodationErrorCode.PROPERTY_NOT_FOUND);

            MappedError result = mapper.map(ex);

            assertThat(result.status()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(result.code()).isEqualTo("ACC-001");
        }

        @Test
        @DisplayName("PartnerNotFoundException이 404로 매핑된다")
        void shouldMapPartnerNotFoundTo404() {
            DomainException ex = createExceptionFrom(PartnerErrorCode.PARTNER_NOT_FOUND);

            MappedError result = mapper.map(ex);

            assertThat(result.status()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(result.code()).isEqualTo("PTN-001");
        }

        @Test
        @DisplayName("RatePlanNotFoundException이 404로 매핑된다")
        void shouldMapRatePlanNotFoundTo404() {
            DomainException ex = createExceptionFrom(PricingErrorCode.RATE_PLAN_NOT_FOUND);

            MappedError result = mapper.map(ex);

            assertThat(result.status()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(result.code()).isEqualTo("PRC-001");
        }

        @Test
        @DisplayName("InventoryExhausted가 409로 매핑된다")
        void shouldMapInventoryExhaustedTo409() {
            DomainException ex = createExceptionFrom(InventoryErrorCode.INVENTORY_EXHAUSTED);

            MappedError result = mapper.map(ex);

            assertThat(result.status()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(result.code()).isEqualTo("INV-002");
        }

        @Test
        @DisplayName("InventoryStopSell이 422로 매핑된다")
        void shouldMapInventoryStopSellTo422() {
            DomainException ex = createExceptionFrom(InventoryErrorCode.INVENTORY_STOP_SELL);

            MappedError result = mapper.map(ex);

            assertThat(result.status()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
            assertThat(result.code()).isEqualTo("INV-003");
        }

        @Test
        @DisplayName("InvalidRateRulePeriod가 400으로 매핑된다")
        void shouldMapInvalidRateRulePeriodTo400() {
            DomainException ex = createExceptionFrom(PricingErrorCode.INVALID_RATE_RULE_PERIOD);

            MappedError result = mapper.map(ex);

            assertThat(result.status()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(result.code()).isEqualTo("PRC-003");
        }
    }

    // =========================================================================
    // map() 테스트 - 응답 필드 검증
    // =========================================================================

    @Nested
    @DisplayName("map - 응답 필드 구성")
    class MapResponseFields {

        @Test
        @DisplayName("code 필드에 도메인 에러 코드가 포함된다")
        void shouldIncludeErrorCode() {
            DomainException ex = createException("ACC-001", "숙소를 찾을 수 없습니다", ErrorCategory.NOT_FOUND);

            MappedError result = mapper.map(ex);

            assertThat(result.code()).isEqualTo("ACC-001");
        }

        @Test
        @DisplayName("title과 detail에 에러 메시지가 포함된다")
        void shouldIncludeErrorMessage() {
            DomainException ex = createException("ACC-001", "숙소를 찾을 수 없습니다", ErrorCategory.NOT_FOUND);

            MappedError result = mapper.map(ex);

            assertThat(result.title()).isEqualTo("숙소를 찾을 수 없습니다");
            assertThat(result.detail()).isEqualTo("숙소를 찾을 수 없습니다");
        }
    }

    // =========================================================================
    // 헬퍼 메서드
    // =========================================================================

    /**
     * 테스트용 DomainException을 생성한다.
     * 임의의 에러 코드와 카테고리를 지정할 수 있다.
     */
    private DomainException createException(String code, String message, ErrorCategory category) {
        ErrorCode errorCode = new ErrorCode() {
            @Override
            public String getCode() { return code; }

            @Override
            public String getMessage() { return message; }

            @Override
            public ErrorCategory getCategory() { return category; }
        };

        return new DomainException(errorCode) {};
    }

    /**
     * 실제 ErrorCode enum으로부터 테스트용 DomainException을 생성한다.
     */
    private DomainException createExceptionFrom(ErrorCode errorCode) {
        return new DomainException(errorCode) {};
    }
}
