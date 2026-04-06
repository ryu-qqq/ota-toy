package com.ryuqq.otatoy.domain.supplier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SupplierTaskTest {

    private static final Instant NOW = SupplierTaskFixture.NOW;
    private static final Instant LATER = SupplierTaskFixture.LATER;

    @Nested
    @DisplayName("T-1: 생성 검증 -- forNew()")
    class Creation {

        @Test
        @DisplayName("정상 생성 시 PENDING 상태이고 id.value()는 null이다")
        void shouldCreateWithPendingStatusAndNullId() {
            SupplierTask task = SupplierTaskFixture.pendingTask();

            assertThat(task.id().value()).isNull();
            assertThat(task.status()).isEqualTo(SupplierTaskStatus.PENDING);
            assertThat(task.supplierId()).isEqualTo(SupplierTaskFixture.DEFAULT_SUPPLIER_ID);
            assertThat(task.supplierApiConfigId()).isEqualTo(SupplierTaskFixture.DEFAULT_API_CONFIG_ID);
            assertThat(task.taskType()).isEqualTo(SupplierTaskType.PROPERTY_CONTENT);
            assertThat(task.payload()).isEqualTo(SupplierTaskFixture.DEFAULT_PAYLOAD);
            assertThat(task.retryCount()).isZero();
            assertThat(task.maxRetries()).isEqualTo(3);
            assertThat(task.failureReason()).isNull();
            assertThat(task.createdAt()).isEqualTo(NOW);
            assertThat(task.processedAt()).isNull();
        }

        @Test
        @DisplayName("payload가 null이어도 생성 가능하다")
        void shouldCreateWithNullPayload() {
            SupplierTask task = SupplierTask.forNew(
                    SupplierTaskFixture.DEFAULT_SUPPLIER_ID,
                    SupplierTaskFixture.DEFAULT_API_CONFIG_ID,
                    SupplierTaskType.RATE_AVAILABILITY, null, 3, NOW);

            assertThat(task.payload()).isNull();
            assertThat(task.taskType()).isEqualTo(SupplierTaskType.RATE_AVAILABILITY);
        }

        @Test
        @DisplayName("supplierId가 null이면 생성 실패")
        void shouldFailWhenSupplierIdIsNull() {
            assertThatThrownBy(() -> SupplierTask.forNew(
                    null, 10L, SupplierTaskType.PROPERTY_CONTENT, null, 3, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("공급자 ID");
        }

        @Test
        @DisplayName("supplierApiConfigId가 null이면 생성 실패")
        void shouldFailWhenApiConfigIdIsNull() {
            assertThatThrownBy(() -> SupplierTask.forNew(
                    SupplierId.of(1L), null, SupplierTaskType.PROPERTY_CONTENT, null, 3, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("API 설정 ID");
        }

        @Test
        @DisplayName("taskType이 null이면 생성 실패")
        void shouldFailWhenTaskTypeIsNull() {
            assertThatThrownBy(() -> SupplierTask.forNew(
                    SupplierId.of(1L), 10L, null, null, 3, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("작업 유형");
        }

        @Test
        @DisplayName("maxRetries가 음수이면 생성 실패")
        void shouldFailWhenMaxRetriesIsNegative() {
            assertThatThrownBy(() -> SupplierTask.forNew(
                    SupplierId.of(1L), 10L, SupplierTaskType.PROPERTY_CONTENT, null, -1, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("최대 재시도");
        }

        @Test
        @DisplayName("now가 null이면 생성 실패")
        void shouldFailWhenNowIsNull() {
            assertThatThrownBy(() -> SupplierTask.forNew(
                    SupplierId.of(1L), 10L, SupplierTaskType.PROPERTY_CONTENT, null, 3, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("생성 시각");
        }
    }

    @Nested
    @DisplayName("T-2: DB 복원 -- reconstitute()")
    class Reconstitution {

        @Test
        @DisplayName("모든 필드가 그대로 복원된다")
        void shouldReconstitute() {
            SupplierTask task = SupplierTask.reconstitute(
                    SupplierTaskId.of(100L), SupplierId.of(1L), 10L,
                    SupplierTaskType.PROPERTY_CONTENT, SupplierTaskStatus.PROCESSING,
                    "{}", 2, 3, "이전 실패 사유", NOW, LATER);

            assertThat(task.id().value()).isEqualTo(100L);
            assertThat(task.status()).isEqualTo(SupplierTaskStatus.PROCESSING);
            assertThat(task.retryCount()).isEqualTo(2);
            assertThat(task.failureReason()).isEqualTo("이전 실패 사유");
            assertThat(task.processedAt()).isEqualTo(LATER);
        }
    }

    @Nested
    @DisplayName("T-3: 상태 전이 -- PENDING -> PROCESSING")
    class MarkProcessing {

        @Test
        @DisplayName("PENDING에서 PROCESSING으로 전이한다")
        void shouldTransitToProcessing() {
            SupplierTask task = SupplierTaskFixture.pendingTask();
            task.markProcessing();

            assertThat(task.status()).isEqualTo(SupplierTaskStatus.PROCESSING);
        }

        @Test
        @DisplayName("COMPLETED에서 PROCESSING 전이 시 예외")
        void shouldFailFromCompleted() {
            SupplierTask task = SupplierTaskFixture.completedTask();

            assertThatThrownBy(task::markProcessing)
                    .isInstanceOf(InvalidSupplierTaskStateTransitionException.class);
        }
    }

    @Nested
    @DisplayName("T-4: 상태 전이 -- PROCESSING -> COMPLETED")
    class MarkCompleted {

        @Test
        @DisplayName("PROCESSING에서 COMPLETED로 전이하고 processedAt을 기록한다")
        void shouldTransitToCompleted() {
            SupplierTask task = SupplierTaskFixture.processingTask();
            task.markCompleted(LATER);

            assertThat(task.status()).isEqualTo(SupplierTaskStatus.COMPLETED);
            assertThat(task.processedAt()).isEqualTo(LATER);
        }

        @Test
        @DisplayName("PENDING에서 COMPLETED 전이 시 예외")
        void shouldFailFromPending() {
            SupplierTask task = SupplierTaskFixture.pendingTask();

            assertThatThrownBy(() -> task.markCompleted(LATER))
                    .isInstanceOf(InvalidSupplierTaskStateTransitionException.class);
        }
    }

    @Nested
    @DisplayName("T-5: 상태 전이 -- PROCESSING -> FAILED")
    class MarkFailed {

        @Test
        @DisplayName("PROCESSING에서 FAILED로 전이하고 retryCount를 증가시킨다")
        void shouldTransitToFailedAndIncrementRetry() {
            SupplierTask task = SupplierTaskFixture.processingTask();
            task.markFailed("HTTP 500 에러", LATER);

            assertThat(task.status()).isEqualTo(SupplierTaskStatus.FAILED);
            assertThat(task.retryCount()).isEqualTo(1);
            assertThat(task.failureReason()).isEqualTo("HTTP 500 에러");
            assertThat(task.processedAt()).isEqualTo(LATER);
        }

        @Test
        @DisplayName("PENDING에서 FAILED 전이 시 예외")
        void shouldFailFromPending() {
            SupplierTask task = SupplierTaskFixture.pendingTask();

            assertThatThrownBy(() -> task.markFailed("실패", LATER))
                    .isInstanceOf(InvalidSupplierTaskStateTransitionException.class);
        }
    }

    @Nested
    @DisplayName("T-6: 재시도 -- FAILED -> PENDING")
    class ResetToPending {

        @Test
        @DisplayName("재시도 가능하면 FAILED에서 PENDING으로 전이한다")
        void shouldResetToPending() {
            SupplierTask task = SupplierTaskFixture.failedTask();
            task.resetToPending();

            assertThat(task.status()).isEqualTo(SupplierTaskStatus.PENDING);
            assertThat(task.processedAt()).isNull();
        }

        @Test
        @DisplayName("재시도 횟수가 소진되면 예외를 발생시킨다")
        void shouldFailWhenRetryExhausted() {
            SupplierTask task = SupplierTaskFixture.failedTaskWithExhaustedRetries();

            assertThat(task.canRetry()).isFalse();
            assertThatThrownBy(task::resetToPending)
                    .isInstanceOf(SupplierTaskRetryExhaustedException.class);
        }

        @Test
        @DisplayName("PROCESSING에서 PENDING 전이 시 예외")
        void shouldFailFromProcessing() {
            SupplierTask task = SupplierTaskFixture.processingTask();

            assertThatThrownBy(task::resetToPending)
                    .isInstanceOf(InvalidSupplierTaskStateTransitionException.class);
        }
    }

    @Nested
    @DisplayName("T-7: canRetry() / isTerminal()")
    class TerminalState {

        @Test
        @DisplayName("COMPLETED는 종료 상태다")
        void completedIsTerminal() {
            SupplierTask task = SupplierTaskFixture.completedTask();

            assertThat(task.isTerminal()).isTrue();
        }

        @Test
        @DisplayName("FAILED이면서 재시도 불가이면 종료 상태다")
        void failedWithExhaustedRetriesIsTerminal() {
            SupplierTask task = SupplierTaskFixture.failedTaskWithExhaustedRetries();

            assertThat(task.isTerminal()).isTrue();
            assertThat(task.canRetry()).isFalse();
        }

        @Test
        @DisplayName("FAILED이면서 재시도 가능하면 종료 상태가 아니다")
        void failedWithRemainingRetriesIsNotTerminal() {
            SupplierTask task = SupplierTaskFixture.failedTask();

            assertThat(task.isTerminal()).isFalse();
            assertThat(task.canRetry()).isTrue();
        }

        @Test
        @DisplayName("PENDING은 종료 상태가 아니다")
        void pendingIsNotTerminal() {
            SupplierTask task = SupplierTaskFixture.pendingTask();

            assertThat(task.isTerminal()).isFalse();
        }

        @Test
        @DisplayName("maxRetries가 0이면 첫 실패에서 종료 상태다")
        void zeroMaxRetriesTerminatesOnFirstFailure() {
            SupplierTask task = SupplierTask.forNew(
                    SupplierId.of(1L), 10L, SupplierTaskType.PROPERTY_CONTENT, null, 0, NOW);
            task.markProcessing();
            task.markFailed("즉시 종료", LATER);

            assertThat(task.isTerminal()).isTrue();
            assertThat(task.canRetry()).isFalse();
        }
    }

    @Nested
    @DisplayName("T-8: equals/hashCode")
    class EqualsHashCode {

        @Test
        @DisplayName("같은 ID면 동등하다")
        void sameIdAreEqual() {
            SupplierTask task1 = SupplierTaskFixture.reconstitutedTask(SupplierTaskStatus.PENDING, 0);
            SupplierTask task2 = SupplierTaskFixture.reconstitutedTask(SupplierTaskStatus.PROCESSING, 1);

            assertThat(task1).isEqualTo(task2);
            assertThat(task1.hashCode()).isEqualTo(task2.hashCode());
        }

        @Test
        @DisplayName("ID가 null이면 동등하지 않다")
        void nullIdAreNotEqual() {
            SupplierTask task1 = SupplierTaskFixture.pendingTask();
            SupplierTask task2 = SupplierTaskFixture.pendingTask();

            assertThat(task1).isNotEqualTo(task2);
        }
    }
}
