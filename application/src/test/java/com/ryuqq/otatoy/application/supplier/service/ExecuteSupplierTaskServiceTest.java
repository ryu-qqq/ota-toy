package com.ryuqq.otatoy.application.supplier.service;

import com.ryuqq.otatoy.application.common.exception.ExternalServiceUnavailableException;
import com.ryuqq.otatoy.application.supplier.dto.SupplierFetchResult;
import com.ryuqq.otatoy.application.supplier.dto.command.ExecuteSupplierTaskCommand;
import com.ryuqq.otatoy.application.supplier.facade.SupplierFetchPersistenceFacade;
import com.ryuqq.otatoy.application.supplier.manager.SupplierApiConfigReadManager;
import com.ryuqq.otatoy.application.supplier.manager.SupplierTaskCommandManager;
import com.ryuqq.otatoy.application.supplier.manager.SupplierTaskReadManager;
import com.ryuqq.otatoy.application.supplier.strategy.SupplierStrategy;
import com.ryuqq.otatoy.application.supplier.strategy.SupplierStrategyProvider;
import com.ryuqq.otatoy.domain.supplier.SupplierApiConfig;
import com.ryuqq.otatoy.domain.supplier.SupplierFixture;
import com.ryuqq.otatoy.domain.supplier.SupplierId;
import com.ryuqq.otatoy.domain.supplier.SupplierTask;
import com.ryuqq.otatoy.domain.supplier.SupplierTaskStatus;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ExecuteSupplierTaskServiceTest {

    @Mock
    SupplierTaskReadManager taskReadManager;

    @Mock
    SupplierTaskCommandManager taskCommandManager;

    @Mock
    SupplierApiConfigReadManager apiConfigReadManager;

    @Mock
    SupplierStrategyProvider strategyProvider;

    @Mock
    SupplierFetchPersistenceFacade fetchFacade;

    @Mock
    SupplierStrategy strategy;

    @InjectMocks
    ExecuteSupplierTaskService service;

    private static final Instant NOW = Instant.parse("2026-04-06T12:00:00Z");
    private static final ExecuteSupplierTaskCommand COMMAND = ExecuteSupplierTaskCommand.of(10, NOW);

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("PENDING Task를 처리하여 Facade에서 원자적으로 완료 처리한다")
        void shouldProcessPendingTaskToCompleted() {
            // given
            SupplierTask task = SupplierFixture.pendingPropertyContentTask();
            SupplierApiConfig config = SupplierFixture.activeApiConfig();
            SupplierFetchResult fetchResult = new SupplierFetchResult(
                    SupplierId.of(1L), "{\"data\":[]}", NOW);

            given(taskReadManager.findPending(10)).willReturn(List.of(task));
            given(apiConfigReadManager.findBySupplierId(any())).willReturn(config);
            given(strategyProvider.getStrategy(config.apiType())).willReturn(strategy);
            given(strategy.fetch(config)).willReturn(fetchResult);

            // when
            service.execute(COMMAND);

            // then — Facade가 원자적으로 처리
            then(fetchFacade).should().completeFetch(eq(fetchResult), eq(task), eq(config.apiType()), eq(NOW));
        }
    }

    @Nested
    @DisplayName("실패 흐름")
    class Failure {

        @Test
        @DisplayName("외부 API 호출 실패 시 Task를 FAILED로 전이하고 실패 사유를 기록한다")
        void shouldMarkTaskAsFailedOnApiError() {
            // given
            SupplierTask task = SupplierFixture.pendingPropertyContentTask();
            SupplierApiConfig config = SupplierFixture.activeApiConfig();

            given(taskReadManager.findPending(10)).willReturn(List.of(task));
            given(apiConfigReadManager.findBySupplierId(any())).willReturn(config);
            given(strategyProvider.getStrategy(config.apiType())).willReturn(strategy);
            willThrow(new RuntimeException("Connection refused")).given(strategy).fetch(config);

            // when
            service.execute(COMMAND);

            // then
            assertThat(task.status()).isEqualTo(SupplierTaskStatus.FAILED);
            assertThat(task.failureReason()).isNotNull();
            then(fetchFacade).should(never()).completeFetch(any(), any(), any(), any());
        }

        @Test
        @DisplayName("CB OPEN 시 Task를 PENDING으로 복귀하고 retryCount를 증가시키지 않는다")
        void shouldDeferRetryOnCircuitBreakerOpen() {
            // given
            SupplierTask task = SupplierFixture.pendingPropertyContentTask();
            SupplierApiConfig config = SupplierFixture.activeApiConfig();

            given(taskReadManager.findPending(10)).willReturn(List.of(task));
            given(apiConfigReadManager.findBySupplierId(any())).willReturn(config);
            given(strategyProvider.getStrategy(config.apiType())).willReturn(strategy);
            willThrow(new ExternalServiceUnavailableException("CB OPEN"))
                    .given(strategy).fetch(config);

            // when
            service.execute(COMMAND);

            // then — PENDING 복귀, retryCount 미증가
            assertThat(task.status()).isEqualTo(SupplierTaskStatus.PENDING);
            then(fetchFacade).should(never()).completeFetch(any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("빈 배치")
    class EmptyBatch {

        @Test
        @DisplayName("PENDING Task가 없으면 아무 것도 하지 않는다")
        void shouldDoNothingWhenNoPendingTasks() {
            // given
            given(taskReadManager.findPending(10)).willReturn(Collections.emptyList());

            // when
            service.execute(COMMAND);

            // then
            then(taskCommandManager).should(never()).persist(any());
            then(fetchFacade).should(never()).completeFetch(any(), any(), any(), any());
        }
    }
}
