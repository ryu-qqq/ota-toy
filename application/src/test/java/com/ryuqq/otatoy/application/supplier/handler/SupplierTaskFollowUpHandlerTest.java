package com.ryuqq.otatoy.application.supplier.handler;

import com.ryuqq.otatoy.application.supplier.factory.SupplierTaskFactory;
import com.ryuqq.otatoy.application.supplier.manager.SupplierApiConfigReadManager;
import com.ryuqq.otatoy.application.supplier.manager.SupplierTaskCommandManager;
import com.ryuqq.otatoy.domain.supplier.SupplierApiConfig;
import com.ryuqq.otatoy.domain.supplier.SupplierFixture;
import com.ryuqq.otatoy.domain.supplier.SupplierTask;
import com.ryuqq.otatoy.domain.supplier.SupplierTaskType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * SupplierTaskFollowUpHandler 단위 테스트.
 * PROPERTY_CONTENT 완료 시 후속 RATE_AVAILABILITY Task 등록을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class SupplierTaskFollowUpHandlerTest {

    @Mock
    SupplierApiConfigReadManager apiConfigReadManager;

    @Mock
    SupplierTaskFactory taskFactory;

    @Mock
    SupplierTaskCommandManager taskCommandManager;

    @InjectMocks
    SupplierTaskFollowUpHandler handler;

    @Nested
    @DisplayName("후속 Task 등록")
    class FollowUp {

        @Test
        @DisplayName("PROPERTY_CONTENT 완료 시 RATE_AVAILABILITY Task를 생성한다")
        void shouldCreateRateAvailabilityTaskAfterPropertyContent() {
            SupplierTask completedTask = SupplierFixture.completedPropertyContentTask();
            SupplierApiConfig config = SupplierFixture.activeApiConfig();
            SupplierTask followUpTask = SupplierFixture.pendingRateAvailabilityTask();

            given(apiConfigReadManager.findBySupplierId(completedTask.supplierId())).willReturn(config);
            given(taskFactory.create(config, SupplierTaskType.RATE_AVAILABILITY)).willReturn(followUpTask);

            handler.handleCompletion(completedTask);

            then(taskCommandManager).should().persist(followUpTask);
        }

        @Test
        @DisplayName("RATE_AVAILABILITY 완료 시에는 후속 Task를 생성하지 않는다")
        void shouldNotCreateFollowUpForRateAvailability() {
            SupplierTask completedTask = SupplierFixture.completedRateAvailabilityTask();

            handler.handleCompletion(completedTask);

            then(apiConfigReadManager).should(never()).findBySupplierId(any());
            then(taskFactory).should(never()).create(any(), any());
            then(taskCommandManager).should(never()).persist(any());
        }
    }
}
