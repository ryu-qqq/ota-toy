package com.ryuqq.otatoy.application.supplier.manager;

import com.ryuqq.otatoy.application.supplier.port.out.SupplierApiConfigQueryPort;
import com.ryuqq.otatoy.application.supplier.port.out.SupplierSyncLogQueryPort;
import com.ryuqq.otatoy.domain.supplier.SupplierApiConfig;
import com.ryuqq.otatoy.domain.supplier.SupplierFixture;
import com.ryuqq.otatoy.domain.supplier.SupplierId;
import com.ryuqq.otatoy.domain.supplier.SupplierSyncLog;
import com.ryuqq.otatoy.domain.supplier.SupplierSyncType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

/**
 * SupplierApiConfigReadManager 단위 테스트.
 * SyncLog 기반 수집 주기 판별 로직과 예외 처리를 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class SupplierApiConfigReadManagerTest {

    @Mock
    SupplierApiConfigQueryPort apiConfigQueryPort;

    @Mock
    SupplierSyncLogQueryPort syncLogQueryPort;

    @InjectMocks
    SupplierApiConfigReadManager manager;

    private static final Instant NOW = Instant.parse("2026-04-06T12:00:00Z");

    @Nested
    @DisplayName("findDueForFetch")
    class FindDueForFetch {

        @Test
        @DisplayName("마지막 수집 이력이 없으면 수집 대상으로 판별한다")
        void shouldReturnConfigWhenNoLastFetch() {
            // given
            SupplierApiConfig config = SupplierFixture.activeApiConfig();
            given(apiConfigQueryPort.findAllActive()).willReturn(List.of(config));
            given(syncLogQueryPort.findLastSuccessBySupplierId(config.supplierId(), SupplierSyncType.FETCH))
                    .willReturn(Optional.empty());

            // when
            List<SupplierApiConfig> result = manager.findDueForFetch(NOW);

            // then
            assertThat(result).containsExactly(config);
        }

        @Test
        @DisplayName("수집 주기가 도래하지 않으면 빈 목록을 반환한다")
        void shouldReturnEmptyWhenNotDue() {
            // given
            SupplierApiConfig config = SupplierFixture.activeApiConfig();
            SupplierSyncLog recentLog = SupplierFixture.successSyncLog();
            // 바로 직전에 수집한 경우 (NOW와 가까운 시각)
            Instant recentFetchTime = NOW.minusSeconds(60); // 1분 전
            SupplierSyncLog recentSyncLog = SupplierSyncLog.forSuccess(
                    config.supplierId(), SupplierSyncType.FETCH, recentFetchTime, 1, 1, 0, 0);

            given(apiConfigQueryPort.findAllActive()).willReturn(List.of(config));
            given(syncLogQueryPort.findLastSuccessBySupplierId(config.supplierId(), SupplierSyncType.FETCH))
                    .willReturn(Optional.of(recentSyncLog));

            // when
            List<SupplierApiConfig> result = manager.findDueForFetch(NOW);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("활성 설정이 없으면 빈 목록을 반환한다")
        void shouldReturnEmptyWhenNoActiveConfigs() {
            // given
            given(apiConfigQueryPort.findAllActive()).willReturn(List.of());

            // when
            List<SupplierApiConfig> result = manager.findDueForFetch(NOW);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllActive")
    class FindAllActive {

        @Test
        @DisplayName("활성 설정 목록을 반환한다")
        void shouldReturnAllActiveConfigs() {
            // given
            List<SupplierApiConfig> configs = List.of(
                    SupplierFixture.apiConfigForSupplier(1L),
                    SupplierFixture.apiConfigForSupplier(2L));
            given(apiConfigQueryPort.findAllActive()).willReturn(configs);

            // when
            List<SupplierApiConfig> result = manager.findAllActive();

            // then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("findBySupplierId")
    class FindBySupplierId {

        @Test
        @DisplayName("존재하는 supplierId로 설정을 조회한다")
        void shouldReturnConfigWhenExists() {
            // given
            SupplierId supplierId = SupplierId.of(1L);
            SupplierApiConfig config = SupplierFixture.activeApiConfig();
            given(apiConfigQueryPort.findBySupplierId(supplierId)).willReturn(Optional.of(config));

            // when
            SupplierApiConfig result = manager.findBySupplierId(supplierId);

            // then
            assertThat(result).isEqualTo(config);
        }

        @Test
        @DisplayName("존재하지 않는 supplierId로 조회 시 IllegalArgumentException 발생")
        void shouldThrowWhenNotFound() {
            // given
            SupplierId supplierId = SupplierId.of(999L);
            given(apiConfigQueryPort.findBySupplierId(supplierId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> manager.findBySupplierId(supplierId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("999");
        }
    }
}
