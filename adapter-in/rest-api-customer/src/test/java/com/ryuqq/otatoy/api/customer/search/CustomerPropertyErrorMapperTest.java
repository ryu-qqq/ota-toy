package com.ryuqq.otatoy.api.customer.search;

import com.ryuqq.otatoy.api.core.ErrorMapper.MappedError;
import com.ryuqq.otatoy.api.customer.common.error.CustomerPropertyErrorMapper;
import com.ryuqq.otatoy.domain.accommodation.AccommodationErrorCode;
import com.ryuqq.otatoy.domain.common.DomainException;
import com.ryuqq.otatoy.domain.common.ErrorCategory;
import com.ryuqq.otatoy.domain.common.ErrorCode;
import com.ryuqq.otatoy.domain.pricing.PricingErrorCode;
import com.ryuqq.otatoy.domain.pricing.RatePlanNotFoundException;
import com.ryuqq.otatoy.domain.property.PropertyNotFoundException;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CustomerPropertyErrorMapper 단위 테스트.
 * ACC-, PRC-, RT- 접두사 DomainException에 대한 supports/map 동작을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
class CustomerPropertyErrorMapperTest {

    private final CustomerPropertyErrorMapper mapper = new CustomerPropertyErrorMapper();

    // =========================================================================
    // supports: 지원 여부 판단
    // =========================================================================

    @Nested
    @DisplayName("supports - 에러 코드 접두사 기반 지원 여부")
    class Supports {

        @Test
        @DisplayName("ACC- 접두사 에러를 지원한다")
        void shouldSupportAccPrefix() {
            DomainException ex = new PropertyNotFoundException();

            assertThat(mapper.supports(ex)).isTrue();
        }

        @Test
        @DisplayName("PRC- 접두사 에러를 지원한다")
        void shouldSupportPrcPrefix() {
            DomainException ex = new RatePlanNotFoundException();

            assertThat(mapper.supports(ex)).isTrue();
        }

        @Test
        @DisplayName("RT- 접두사 에러를 지원한다")
        void shouldSupportRtPrefix() {
            DomainException ex = new RoomTypeNotFoundException();

            // RoomTypeNotFoundException은 ACC-002 코드를 사용하므로 ACC- 접두사로 지원됨
            assertThat(mapper.supports(ex)).isTrue();
        }

        @Test
        @DisplayName("RSV- 접두사 에러는 지원하지 않는다")
        void shouldNotSupportRsvPrefix() {
            DomainException ex = createException("RSV-001", "예약 없음", ErrorCategory.NOT_FOUND);

            assertThat(mapper.supports(ex)).isFalse();
        }

        @Test
        @DisplayName("INV- 접두사 에러는 지원하지 않는다")
        void shouldNotSupportInvPrefix() {
            DomainException ex = createException("INV-001", "재고 없음", ErrorCategory.NOT_FOUND);

            assertThat(mapper.supports(ex)).isFalse();
        }

        @Test
        @DisplayName("알 수 없는 접두사 에러는 지원하지 않는다")
        void shouldNotSupportUnknownPrefix() {
            DomainException ex = createException("XXX-001", "알 수 없음", ErrorCategory.NOT_FOUND);

            assertThat(mapper.supports(ex)).isFalse();
        }
    }

    // =========================================================================
    // map: ErrorCategory -> HttpStatus 매핑
    // =========================================================================

    @Nested
    @DisplayName("map - ErrorCategory 기반 HTTP 상태 매핑")
    class Map {

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("categoryToStatusProvider")
        @DisplayName("ErrorCategory별 HttpStatus가 올바르게 매핑된다")
        void shouldMapCategoryToHttpStatus(ErrorCategory category, HttpStatus expectedStatus) {
            DomainException ex = createException("ACC-999", "테스트 메시지", category);

            MappedError error = mapper.map(ex);

            assertThat(error.status()).isEqualTo(expectedStatus);
        }

        static Stream<Arguments> categoryToStatusProvider() {
            return Stream.of(
                Arguments.of(ErrorCategory.NOT_FOUND, HttpStatus.NOT_FOUND),
                Arguments.of(ErrorCategory.VALIDATION, HttpStatus.BAD_REQUEST),
                Arguments.of(ErrorCategory.CONFLICT, HttpStatus.CONFLICT),
                Arguments.of(ErrorCategory.FORBIDDEN, HttpStatus.UNPROCESSABLE_ENTITY)
            );
        }

        @Test
        @DisplayName("에러 코드가 MappedError에 올바르게 포함된다")
        void shouldIncludeErrorCode() {
            DomainException ex = new PropertyNotFoundException();

            MappedError error = mapper.map(ex);

            assertThat(error.code()).isEqualTo("ACC-001");
        }

        @Test
        @DisplayName("에러 메시지가 title과 detail에 모두 포함된다")
        void shouldIncludeErrorMessage() {
            DomainException ex = new PropertyNotFoundException();

            MappedError error = mapper.map(ex);

            assertThat(error.title()).isEqualTo("숙소를 찾을 수 없습니다");
            assertThat(error.detail()).isEqualTo("숙소를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("PRC- 에러에 대해 NOT_FOUND가 매핑된다")
        void shouldMapPrcNotFound() {
            DomainException ex = new RatePlanNotFoundException();

            MappedError error = mapper.map(ex);

            assertThat(error.status()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(error.code()).isEqualTo("PRC-001");
        }
    }

    // =========================================================================
    // 헬퍼 메서드
    // =========================================================================

    /**
     * 테스트용 DomainException 생성.
     * 도메인 예외의 protected 생성자를 우회하여 원하는 코드/카테고리를 지정한다.
     */
    private static DomainException createException(String code, String message, ErrorCategory category) {
        ErrorCode errorCode = new ErrorCode() {
            @Override public String getCode() { return code; }
            @Override public String getMessage() { return message; }
            @Override public ErrorCategory getCategory() { return category; }
        };

        return new DomainException(errorCode) {};
    }
}
