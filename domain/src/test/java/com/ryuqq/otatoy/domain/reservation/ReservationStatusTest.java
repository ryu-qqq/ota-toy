package com.ryuqq.otatoy.domain.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationStatusTest {

    @Nested
    @DisplayName("T-5: Enum VO 검증")
    class EnumValidation {

        @Test
        @DisplayName("PENDING의 displayName은 '대기'이다")
        void pendingDisplayName() {
            assertThat(ReservationStatus.PENDING.displayName()).isEqualTo("대기");
        }

        @Test
        @DisplayName("CONFIRMED의 displayName은 '확정'이다")
        void confirmedDisplayName() {
            assertThat(ReservationStatus.CONFIRMED.displayName()).isEqualTo("확정");
        }

        @Test
        @DisplayName("CANCELLED의 displayName은 '취소'이다")
        void cancelledDisplayName() {
            assertThat(ReservationStatus.CANCELLED.displayName()).isEqualTo("취소");
        }

        @Test
        @DisplayName("COMPLETED의 displayName은 '완료'이다")
        void completedDisplayName() {
            assertThat(ReservationStatus.COMPLETED.displayName()).isEqualTo("완료");
        }

        @Test
        @DisplayName("NO_SHOW의 displayName은 '노쇼'이다")
        void noShowDisplayName() {
            assertThat(ReservationStatus.NO_SHOW.displayName()).isEqualTo("노쇼");
        }

        @Test
        @DisplayName("모든 상태는 5개이다")
        void shouldHaveFiveStatuses() {
            assertThat(ReservationStatus.values()).hasSize(5);
        }

        @Test
        @DisplayName("모든 상태에 displayName이 null이 아니고 비어있지 않다")
        void allStatusesShouldHaveNonEmptyDisplayName() {
            for (ReservationStatus status : ReservationStatus.values()) {
                assertThat(status.displayName())
                        .as("상태 %s의 displayName", status.name())
                        .isNotNull()
                        .isNotBlank();
            }
        }
    }
}
