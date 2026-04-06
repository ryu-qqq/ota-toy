package com.ryuqq.otatoy.api.customer.reservation;

import com.ryuqq.otatoy.api.core.ErrorMapper.MappedError;
import com.ryuqq.otatoy.domain.common.DomainException;
import com.ryuqq.otatoy.domain.common.ErrorCategory;
import com.ryuqq.otatoy.domain.common.ErrorCode;
import com.ryuqq.otatoy.domain.inventory.InventoryErrorCode;
import com.ryuqq.otatoy.domain.reservation.ReservationErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CustomerReservationErrorMapper 단위 테스트.
 * RSV-, INV- 접두사 DomainException에 대한 supports/map 동작을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
class CustomerReservationErrorMapperTest {

    private final CustomerReservationErrorMapper mapper = new CustomerReservationErrorMapper();

    // =========================================================================
    // supports: 지원 여부 판단
    // =========================================================================

    @Nested
    @DisplayName("supports - 에러 코드 접두사 기반 지원 여부")
    class Supports {

        @Test
        @DisplayName("RSV- 접두사 에러를 지원한다")
        void shouldSupportRsvPrefix() {
            DomainException ex = createException(ReservationErrorCode.RESERVATION_NOT_FOUND);

            assertThat(mapper.supports(ex)).isTrue();
        }

        @Test
        @DisplayName("INV- 접두사 에러를 지원한다")
        void shouldSupportInvPrefix() {
            DomainException ex = createException(InventoryErrorCode.INVENTORY_NOT_FOUND);

            assertThat(mapper.supports(ex)).isTrue();
        }

        @Test
        @DisplayName("ACC- 접두사 에러는 지원하지 않는다")
        void shouldNotSupportAccPrefix() {
            DomainException ex = createExceptionWithCode("ACC-001", "숙소 없음", ErrorCategory.NOT_FOUND);

            assertThat(mapper.supports(ex)).isFalse();
        }

        @Test
        @DisplayName("PRC- 접두사 에러는 지원하지 않는다")
        void shouldNotSupportPrcPrefix() {
            DomainException ex = createExceptionWithCode("PRC-001", "요금 없음", ErrorCategory.NOT_FOUND);

            assertThat(mapper.supports(ex)).isFalse();
        }

        @ParameterizedTest(name = "RSV 에러코드: {0}")
        @MethodSource("allReservationErrorCodes")
        @DisplayName("모든 예약 에러코드를 지원한다")
        void shouldSupportAllReservationErrorCodes(ReservationErrorCode errorCode) {
            DomainException ex = createException(errorCode);

            assertThat(mapper.supports(ex)).isTrue();
        }

        static Stream<ReservationErrorCode> allReservationErrorCodes() {
            return Stream.of(ReservationErrorCode.values());
        }

        @ParameterizedTest(name = "INV 에러코드: {0}")
        @MethodSource("allInventoryErrorCodes")
        @DisplayName("모든 재고 에러코드를 지원한다")
        void shouldSupportAllInventoryErrorCodes(InventoryErrorCode errorCode) {
            DomainException ex = createException(errorCode);

            assertThat(mapper.supports(ex)).isTrue();
        }

        static Stream<InventoryErrorCode> allInventoryErrorCodes() {
            return Stream.of(InventoryErrorCode.values());
        }
    }

    // =========================================================================
    // map: ErrorCategory -> HttpStatus 매핑
    // =========================================================================

    @Nested
    @DisplayName("map - ErrorCategory 기반 HTTP 상태 매핑")
    class MapTest {

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("categoryToStatusProvider")
        @DisplayName("ErrorCategory별 HttpStatus가 올바르게 매핑된다")
        void shouldMapCategoryToHttpStatus(ErrorCategory category, HttpStatus expectedStatus) {
            DomainException ex = createExceptionWithCode("RSV-999", "테스트", category);

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
        @DisplayName("RSV-001 NOT_FOUND -> 404 NOT_FOUND")
        void shouldMapReservationNotFound() {
            DomainException ex = createException(ReservationErrorCode.RESERVATION_NOT_FOUND);

            MappedError error = mapper.map(ex);

            assertThat(error.status()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(error.code()).isEqualTo("RSV-001");
            assertThat(error.title()).isEqualTo("예약을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("RSV-003 CONFLICT -> 409 CONFLICT")
        void shouldMapReservationAlreadyCancelled() {
            DomainException ex = createException(ReservationErrorCode.RESERVATION_ALREADY_CANCELLED);

            MappedError error = mapper.map(ex);

            assertThat(error.status()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(error.code()).isEqualTo("RSV-003");
        }

        @Test
        @DisplayName("RSV-006 CONFLICT -> 409 CONFLICT (세션 만료)")
        void shouldMapSessionExpired() {
            DomainException ex = createException(ReservationErrorCode.RESERVATION_SESSION_EXPIRED);

            MappedError error = mapper.map(ex);

            assertThat(error.status()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(error.code()).isEqualTo("RSV-006");
        }

        @Test
        @DisplayName("INV-002 CONFLICT -> 409 CONFLICT (재고 소진)")
        void shouldMapInventoryExhausted() {
            DomainException ex = createException(InventoryErrorCode.INVENTORY_EXHAUSTED);

            MappedError error = mapper.map(ex);

            assertThat(error.status()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(error.code()).isEqualTo("INV-002");
        }

        @Test
        @DisplayName("INV-003 FORBIDDEN -> 422 UNPROCESSABLE_ENTITY (판매 중지)")
        void shouldMapInventoryStopSell() {
            DomainException ex = createException(InventoryErrorCode.INVENTORY_STOP_SELL);

            MappedError error = mapper.map(ex);

            assertThat(error.status()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
            assertThat(error.code()).isEqualTo("INV-003");
        }

        @Test
        @DisplayName("INV-004 VALIDATION -> 400 BAD_REQUEST")
        void shouldMapInventoryOverflow() {
            DomainException ex = createException(InventoryErrorCode.INVENTORY_OVERFLOW);

            MappedError error = mapper.map(ex);

            assertThat(error.status()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(error.code()).isEqualTo("INV-004");
        }
    }

    // =========================================================================
    // map: detail 필드 — args 유무에 따른 동작
    // =========================================================================

    @Nested
    @DisplayName("map - detail 필드 결정 로직")
    class MapDetail {

        @Test
        @DisplayName("args가 비어있으면 errorMessage가 detail이 된다")
        void shouldUseErrorMessageWhenNoArgs() {
            DomainException ex = createException(ReservationErrorCode.RESERVATION_NOT_FOUND);

            MappedError error = mapper.map(ex);

            assertThat(error.detail()).isEqualTo("예약을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("args가 있으면 args.toString()이 detail이 된다")
        void shouldUseArgsToStringWhenArgsPresent() {
            DomainException ex = createExceptionWithArgs(
                "RSV-001", "예약 없음", ErrorCategory.NOT_FOUND,
                Map.of("reservationId", 100L)
            );

            MappedError error = mapper.map(ex);

            assertThat(error.detail()).contains("reservationId");
            assertThat(error.detail()).contains("100");
        }
    }

    // =========================================================================
    // 헬퍼 메서드
    // =========================================================================

    private static DomainException createException(ErrorCode errorCode) {
        return new DomainException(errorCode) {};
    }

    private static DomainException createExceptionWithCode(
            String code, String message, ErrorCategory category) {
        ErrorCode errorCode = new ErrorCode() {
            @Override public String getCode() { return code; }
            @Override public String getMessage() { return message; }
            @Override public ErrorCategory getCategory() { return category; }
        };
        return new DomainException(errorCode) {};
    }

    private static DomainException createExceptionWithArgs(
            String code, String message, ErrorCategory category, Map<String, Object> args) {
        ErrorCode errorCode = new ErrorCode() {
            @Override public String getCode() { return code; }
            @Override public String getMessage() { return message; }
            @Override public ErrorCategory getCategory() { return category; }
        };
        return new DomainException(errorCode, args) {};
    }
}
