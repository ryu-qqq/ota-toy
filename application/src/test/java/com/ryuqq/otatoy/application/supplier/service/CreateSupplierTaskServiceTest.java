package com.ryuqq.otatoy.application.supplier.service;

import com.ryuqq.otatoy.application.supplier.dto.command.CreateSupplierTaskCommand;
import com.ryuqq.otatoy.application.supplier.factory.SupplierTaskFactory;
import com.ryuqq.otatoy.application.supplier.manager.SupplierApiConfigReadManager;
import com.ryuqq.otatoy.application.supplier.manager.SupplierTaskCommandManager;
import com.ryuqq.otatoy.application.supplier.manager.SupplierTaskReadManager;
import com.ryuqq.otatoy.domain.supplier.SupplierApiConfig;
import com.ryuqq.otatoy.domain.supplier.SupplierFixture;
import com.ryuqq.otatoy.domain.supplier.SupplierTasks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CreateSupplierTaskServiceTest {

    @Mock
    SupplierApiConfigReadManager apiConfigReadManager;

    @Mock
    SupplierTaskFactory taskFactory;

    @Mock
    SupplierTaskReadManager taskReadManager;

    @Mock
    SupplierTaskCommandManager taskCommandManager;

    @InjectMocks
    CreateSupplierTaskService service;

    private static final Instant NOW = Instant.parse("2026-04-06T12:00:00Z");

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("수집 주기 도래 + 중복 없으면 Task를 생성하고 저장한다")
        void shouldCreateTasksWhenDueAndNoDuplicates() {
            // given
            List<SupplierApiConfig> dueConfigs = List.of(SupplierFixture.activeApiConfig());
            SupplierTasks candidates = SupplierTasks.from(
                    List.of(SupplierFixture.pendingPropertyContentTask()));
            SupplierTasks inProgress = SupplierTasks.from(List.of());

            given(apiConfigReadManager.findDueForFetch(NOW)).willReturn(dueConfigs);
            given(taskFactory.createCandidates(dueConfigs)).willReturn(candidates);
            given(taskReadManager.findInProgress()).willReturn(inProgress);

            // when
            service.execute(CreateSupplierTaskCommand.of(NOW));

            // then
            then(taskCommandManager).should().persistAll(candidates.items());
        }

        @Test
        @DisplayName("이미 진행 중인 Task와 동일한 supplierId+taskType은 저장하지 않는다")
        void shouldExcludeDuplicateTasks() {
            // given
            List<SupplierApiConfig> dueConfigs = List.of(SupplierFixture.activeApiConfig());
            SupplierTasks candidates = SupplierTasks.from(
                    List.of(SupplierFixture.pendingPropertyContentTask()));
            SupplierTasks inProgress = SupplierTasks.from(
                    List.of(SupplierFixture.pendingPropertyContentTask()));

            given(apiConfigReadManager.findDueForFetch(NOW)).willReturn(dueConfigs);
            given(taskFactory.createCandidates(dueConfigs)).willReturn(candidates);
            given(taskReadManager.findInProgress()).willReturn(inProgress);

            // when
            service.execute(CreateSupplierTaskCommand.of(NOW));

            // then
            then(taskCommandManager).should(never()).persistAll(anyList());
        }
    }

    @Nested
    @DisplayName("조기 종료")
    class EarlyReturn {

        @Test
        @DisplayName("수집 주기 도래한 설정이 없으면 Factory를 호출하지 않는다")
        void shouldSkipWhenNoDueConfigs() {
            // given
            given(apiConfigReadManager.findDueForFetch(NOW)).willReturn(Collections.emptyList());

            // when
            service.execute(CreateSupplierTaskCommand.of(NOW));

            // then
            then(taskFactory).should(never()).createCandidates(anyList());
            then(taskCommandManager).should(never()).persistAll(anyList());
        }
    }
}
