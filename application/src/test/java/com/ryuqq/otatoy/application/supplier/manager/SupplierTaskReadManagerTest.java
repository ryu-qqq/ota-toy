package com.ryuqq.otatoy.application.supplier.manager;

import com.ryuqq.otatoy.application.supplier.port.out.SupplierTaskQueryPort;
import com.ryuqq.otatoy.domain.supplier.SupplierFixture;
import com.ryuqq.otatoy.domain.supplier.SupplierTask;
import com.ryuqq.otatoy.domain.supplier.SupplierTaskStatus;
import com.ryuqq.otatoy.domain.supplier.SupplierTasks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * SupplierTaskReadManager 단위 테스트.
 * PENDING+PROCESSING 합산 조회(findInProgress)와 배치 조회를 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class SupplierTaskReadManagerTest {

    @Mock
    SupplierTaskQueryPort taskQueryPort;

    @InjectMocks
    SupplierTaskReadManager manager;

    @Nested
    @DisplayName("findPending")
    class FindPending {

        @Test
        @DisplayName("PENDING 상태의 Task를 배치 크기만큼 조회한다")
        void shouldReturnPendingTasks() {
            // given
            int batchSize = 10;
            List<SupplierTask> tasks = List.of(SupplierFixture.pendingPropertyContentTask());
            given(taskQueryPort.findByStatus(SupplierTaskStatus.PENDING, batchSize)).willReturn(tasks);

            // when
            List<SupplierTask> result = manager.findPending(batchSize);

            // then
            assertThat(result).hasSize(1);
            then(taskQueryPort).should().findByStatus(SupplierTaskStatus.PENDING, batchSize);
        }
    }

    @Nested
    @DisplayName("findFailedRetryable")
    class FindFailedRetryable {

        @Test
        @DisplayName("재시도 가능한 FAILED Task를 조회한다")
        void shouldReturnFailedRetryableTasks() {
            // given
            int limit = 5;
            List<SupplierTask> tasks = List.of(SupplierFixture.failedRetryableTask());
            given(taskQueryPort.findFailedRetryable(limit)).willReturn(tasks);

            // when
            List<SupplierTask> result = manager.findFailedRetryable(limit);

            // then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("findInProgress")
    class FindInProgress {

        @Test
        @DisplayName("PENDING과 PROCESSING 상태의 Task를 합산하여 SupplierTasks로 반환한다")
        void shouldCombinePendingAndProcessingTasks() {
            // given
            SupplierTask pendingTask = SupplierFixture.pendingPropertyContentTask();
            SupplierTask processingTask = SupplierFixture.pendingRateAvailabilityTask();

            given(taskQueryPort.findByStatus(SupplierTaskStatus.PENDING, Integer.MAX_VALUE))
                    .willReturn(List.of(pendingTask));
            given(taskQueryPort.findByStatus(SupplierTaskStatus.PROCESSING, Integer.MAX_VALUE))
                    .willReturn(List.of(processingTask));

            // when
            SupplierTasks result = manager.findInProgress();

            // then
            assertThat(result.items()).hasSize(2);
            then(taskQueryPort).should().findByStatus(SupplierTaskStatus.PENDING, Integer.MAX_VALUE);
            then(taskQueryPort).should().findByStatus(SupplierTaskStatus.PROCESSING, Integer.MAX_VALUE);
        }

        @Test
        @DisplayName("진행 중인 Task가 없으면 빈 SupplierTasks를 반환한다")
        void shouldReturnEmptyTasksWhenNoneInProgress() {
            // given
            given(taskQueryPort.findByStatus(SupplierTaskStatus.PENDING, Integer.MAX_VALUE))
                    .willReturn(List.of());
            given(taskQueryPort.findByStatus(SupplierTaskStatus.PROCESSING, Integer.MAX_VALUE))
                    .willReturn(List.of());

            // when
            SupplierTasks result = manager.findInProgress();

            // then
            assertThat(result.items()).isEmpty();
        }
    }
}
