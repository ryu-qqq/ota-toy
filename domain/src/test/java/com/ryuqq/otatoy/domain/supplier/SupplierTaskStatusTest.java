package com.ryuqq.otatoy.domain.supplier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SupplierTaskStatusTest {

    @Nested
    @DisplayName("허용된 전이")
    class AllowedTransitions {

        @Test
        @DisplayName("PENDING -> PROCESSING")
        void pendingToProcessing() {
            assertThat(SupplierTaskStatus.PENDING.canTransitTo(SupplierTaskStatus.PROCESSING)).isTrue();
            assertThat(SupplierTaskStatus.PENDING.transitTo(SupplierTaskStatus.PROCESSING))
                    .isEqualTo(SupplierTaskStatus.PROCESSING);
        }

        @Test
        @DisplayName("PROCESSING -> COMPLETED")
        void processingToCompleted() {
            assertThat(SupplierTaskStatus.PROCESSING.canTransitTo(SupplierTaskStatus.COMPLETED)).isTrue();
            assertThat(SupplierTaskStatus.PROCESSING.transitTo(SupplierTaskStatus.COMPLETED))
                    .isEqualTo(SupplierTaskStatus.COMPLETED);
        }

        @Test
        @DisplayName("PROCESSING -> FAILED")
        void processingToFailed() {
            assertThat(SupplierTaskStatus.PROCESSING.canTransitTo(SupplierTaskStatus.FAILED)).isTrue();
            assertThat(SupplierTaskStatus.PROCESSING.transitTo(SupplierTaskStatus.FAILED))
                    .isEqualTo(SupplierTaskStatus.FAILED);
        }

        @Test
        @DisplayName("FAILED -> PENDING (재시도)")
        void failedToPending() {
            assertThat(SupplierTaskStatus.FAILED.canTransitTo(SupplierTaskStatus.PENDING)).isTrue();
            assertThat(SupplierTaskStatus.FAILED.transitTo(SupplierTaskStatus.PENDING))
                    .isEqualTo(SupplierTaskStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("불허 전이")
    class DisallowedTransitions {

        @Test
        @DisplayName("PENDING -> COMPLETED 불가")
        void pendingToCompleted() {
            assertThat(SupplierTaskStatus.PENDING.canTransitTo(SupplierTaskStatus.COMPLETED)).isFalse();
            assertThatThrownBy(() -> SupplierTaskStatus.PENDING.transitTo(SupplierTaskStatus.COMPLETED))
                    .isInstanceOf(InvalidSupplierTaskStateTransitionException.class);
        }

        @Test
        @DisplayName("PENDING -> FAILED 불가")
        void pendingToFailed() {
            assertThat(SupplierTaskStatus.PENDING.canTransitTo(SupplierTaskStatus.FAILED)).isFalse();
        }

        @Test
        @DisplayName("COMPLETED는 종료 상태이므로 전이 불가")
        void completedIsTerminal() {
            for (SupplierTaskStatus target : SupplierTaskStatus.values()) {
                assertThat(SupplierTaskStatus.COMPLETED.canTransitTo(target)).isFalse();
            }
        }

        @Test
        @DisplayName("FAILED -> COMPLETED 불가")
        void failedToCompleted() {
            assertThat(SupplierTaskStatus.FAILED.canTransitTo(SupplierTaskStatus.COMPLETED)).isFalse();
        }

        @Test
        @DisplayName("FAILED -> PROCESSING 불가")
        void failedToProcessing() {
            assertThat(SupplierTaskStatus.FAILED.canTransitTo(SupplierTaskStatus.PROCESSING)).isFalse();
        }
    }

    @Nested
    @DisplayName("displayName()")
    class DisplayNameTest {

        @ParameterizedTest
        @EnumSource(SupplierTaskStatus.class)
        @DisplayName("모든 상태에 displayName이 존재한다")
        void allStatusHaveDisplayName(SupplierTaskStatus status) {
            assertThat(status.displayName()).isNotBlank();
        }
    }
}
