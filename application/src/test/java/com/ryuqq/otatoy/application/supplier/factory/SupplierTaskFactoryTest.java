package com.ryuqq.otatoy.application.supplier.factory;

import com.ryuqq.otatoy.application.common.factory.TimeProvider;
import com.ryuqq.otatoy.domain.supplier.SupplierApiConfig;
import com.ryuqq.otatoy.domain.supplier.SupplierFixture;
import com.ryuqq.otatoy.domain.supplier.SupplierTask;
import com.ryuqq.otatoy.domain.supplier.SupplierTaskStatus;
import com.ryuqq.otatoy.domain.supplier.SupplierTaskType;
import com.ryuqq.otatoy.domain.supplier.SupplierTasks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * SupplierTaskFactory 단위 테스트.
 * TimeProvider로부터 시간을 받아 Task를 올바르게 생성하는지 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class SupplierTaskFactoryTest {

    @Mock
    TimeProvider timeProvider;

    @InjectMocks
    SupplierTaskFactory factory;

    private static final Instant NOW = Instant.parse("2026-04-06T12:00:00Z");

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("지정 TaskType으로 PENDING 상태 Task를 생성한다")
        void shouldCreatePendingTask() {
            // given
            given(timeProvider.now()).willReturn(NOW);
            SupplierApiConfig config = SupplierFixture.activeApiConfig();

            // when
            SupplierTask task = factory.create(config, SupplierTaskType.PROPERTY_CONTENT);

            // then
            assertThat(task.supplierId()).isEqualTo(config.supplierId());
            assertThat(task.supplierApiConfigId()).isEqualTo(config.id());
            assertThat(task.taskType()).isEqualTo(SupplierTaskType.PROPERTY_CONTENT);
            assertThat(task.status()).isEqualTo(SupplierTaskStatus.PENDING);
            assertThat(task.createdAt()).isEqualTo(NOW);
        }
    }

    @Nested
    @DisplayName("createCandidates")
    class CreateAll {

        @Test
        @DisplayName("설정 1개에 대해 TaskType 수만큼 Task를 생성한다")
        void shouldCreateTasksForAllTaskTypes() {
            // given
            given(timeProvider.now()).willReturn(NOW);
            SupplierApiConfig config = SupplierFixture.activeApiConfig();

            // when
            SupplierTasks tasks = factory.createCandidates(List.of(config));

            // then
            assertThat(tasks.items()).hasSize(SupplierTaskType.values().length);
            assertThat(tasks.items()).extracting(SupplierTask::taskType)
                    .containsExactly(SupplierTaskType.PROPERTY_CONTENT, SupplierTaskType.RATE_AVAILABILITY);
            assertThat(tasks.items()).allMatch(t -> t.status() == SupplierTaskStatus.PENDING);
        }

        @Test
        @DisplayName("설정 여러 개에 대해 각각 TaskType 수만큼 Task를 생성한다")
        void shouldCreateTasksForMultipleConfigs() {
            // given
            given(timeProvider.now()).willReturn(NOW);
            SupplierApiConfig config1 = SupplierFixture.apiConfigForSupplier(1L);
            SupplierApiConfig config2 = SupplierFixture.apiConfigForSupplier(2L);

            // when
            SupplierTasks tasks = factory.createCandidates(List.of(config1, config2));

            // then
            int expectedCount = 2 * SupplierTaskType.values().length;
            assertThat(tasks.items()).hasSize(expectedCount);
        }
    }
}
