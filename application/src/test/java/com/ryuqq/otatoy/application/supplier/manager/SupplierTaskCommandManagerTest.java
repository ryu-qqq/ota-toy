package com.ryuqq.otatoy.application.supplier.manager;

import com.ryuqq.otatoy.application.supplier.port.out.SupplierTaskCommandPort;
import com.ryuqq.otatoy.domain.supplier.SupplierFixture;
import com.ryuqq.otatoy.domain.supplier.SupplierTask;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.BDDMockito.then;

/**
 * SupplierTaskCommandManager 단위 테스트.
 * Task 저장이 올바르게 Port에 위임되는지 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class SupplierTaskCommandManagerTest {

    @Mock
    SupplierTaskCommandPort taskCommandPort;

    @InjectMocks
    SupplierTaskCommandManager manager;

    @Nested
    @DisplayName("persist")
    class Persist {

        @Test
        @DisplayName("Task 1건을 Port에 위임하여 저장한다")
        void shouldDelegatePersistToPort() {
            // given
            SupplierTask task = SupplierFixture.pendingPropertyContentTask();

            // when
            manager.persist(task);

            // then
            then(taskCommandPort).should().persist(task);
        }
    }

    @Nested
    @DisplayName("persistAll")
    class PersistAll {

        @Test
        @DisplayName("Task 여러 건을 Port에 위임하여 일괄 저장한다")
        void shouldDelegatePersistAllToPort() {
            // given
            List<SupplierTask> tasks = List.of(
                    SupplierFixture.pendingPropertyContentTask(),
                    SupplierFixture.pendingRateAvailabilityTask());

            // when
            manager.persistAll(tasks);

            // then
            then(taskCommandPort).should().persistAll(tasks);
        }
    }
}
