package com.ryuqq.otatoy.domain.reservation;

import com.ryuqq.otatoy.domain.common.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationErrorCodeTest {

    @Nested
    @DisplayName("T-5: ErrorCode Enum 검증")
    class ErrorCodeValidation {

        @Test
        @DisplayName("RESERVATION_NOT_FOUND: RSV-001, 404")
        void reservationNotFound() {
            ReservationErrorCode code = ReservationErrorCode.RESERVATION_NOT_FOUND;

            assertThat(code.getCode()).isEqualTo("RSV-001");
            assertThat(code.getMessage()).isEqualTo("예약을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("INVALID_RESERVATION_STATE: RSV-002, 400")
        void invalidReservationState() {
            ReservationErrorCode code = ReservationErrorCode.INVALID_RESERVATION_STATE;

            assertThat(code.getCode()).isEqualTo("RSV-002");
            assertThat(code.getMessage()).isEqualTo("유효하지 않은 예약 상태 전이입니다");
        }

        @Test
        @DisplayName("RESERVATION_ALREADY_CANCELLED: RSV-003, 409")
        void reservationAlreadyCancelled() {
            ReservationErrorCode code = ReservationErrorCode.RESERVATION_ALREADY_CANCELLED;

            assertThat(code.getCode()).isEqualTo("RSV-003");
            assertThat(code.getMessage()).isEqualTo("이미 취소된 예약입니다");
        }

        @Test
        @DisplayName("RESERVATION_ALREADY_COMPLETED: RSV-004, 409")
        void reservationAlreadyCompleted() {
            ReservationErrorCode code = ReservationErrorCode.RESERVATION_ALREADY_COMPLETED;

            assertThat(code.getCode()).isEqualTo("RSV-004");
            assertThat(code.getMessage()).isEqualTo("이미 완료된 예약입니다");
        }

        @Test
        @DisplayName("모든 ErrorCode는 ErrorCode 인터페이스를 구현한다")
        void shouldImplementErrorCodeInterface() {
            for (ReservationErrorCode code : ReservationErrorCode.values()) {
                assertThat(code).isInstanceOf(ErrorCode.class);
            }
        }

        @Test
        @DisplayName("모든 ErrorCode의 code는 RSV- 접두사로 시작한다")
        void allCodesShouldStartWithRsvPrefix() {
            for (ReservationErrorCode code : ReservationErrorCode.values()) {
                assertThat(code.getCode())
                        .as("ErrorCode %s의 code", code.name())
                        .startsWith("RSV-");
            }
        }

        @Test
        @DisplayName("모든 ErrorCode의 message는 null이 아니고 비어있지 않다")
        void allCodesShouldHaveNonEmptyMessage() {
            for (ReservationErrorCode code : ReservationErrorCode.values()) {
                assertThat(code.getMessage())
                        .as("ErrorCode %s의 message", code.name())
                        .isNotNull()
                        .isNotBlank();
            }
        }
    }
}
