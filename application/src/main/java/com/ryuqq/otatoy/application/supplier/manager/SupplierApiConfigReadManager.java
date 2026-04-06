package com.ryuqq.otatoy.application.supplier.manager;

import com.ryuqq.otatoy.application.supplier.port.out.SupplierApiConfigQueryPort;
import com.ryuqq.otatoy.application.supplier.port.out.SupplierSyncLogQueryPort;
import com.ryuqq.otatoy.domain.supplier.SupplierApiConfig;
import com.ryuqq.otatoy.domain.supplier.SupplierId;
import com.ryuqq.otatoy.domain.supplier.SupplierSyncLog;
import com.ryuqq.otatoy.domain.supplier.SupplierSyncType;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * 공급자 API 설정 조회 관리자.
 * SyncLog 기반으로 수집 대상을 판별한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierApiConfigReadManager {

    private final SupplierApiConfigQueryPort apiConfigQueryPort;
    private final SupplierSyncLogQueryPort syncLogQueryPort;

    public SupplierApiConfigReadManager(SupplierApiConfigQueryPort apiConfigQueryPort,
                                         SupplierSyncLogQueryPort syncLogQueryPort) {
        this.apiConfigQueryPort = apiConfigQueryPort;
        this.syncLogQueryPort = syncLogQueryPort;
    }

    /**
     * 수집 주기가 도래한 설정 목록을 반환한다.
     * SyncLog에서 마지막 FETCH SUCCESS 시각을 조회하여 isDueForFetch()에 전달한다.
     */
    public List<SupplierApiConfig> findDueForFetch(Instant now) {
        return apiConfigQueryPort.findAllActive().stream()
                .filter(config -> {
                    Instant lastFetchedAt = syncLogQueryPort
                            .findLastSuccessBySupplierId(config.supplierId(), SupplierSyncType.FETCH)
                            .map(SupplierSyncLog::syncedAt)
                            .orElse(null);
                    return config.isDueForFetch(lastFetchedAt, now);
                })
                .toList();
    }

    public List<SupplierApiConfig> findAllActive() {
        return apiConfigQueryPort.findAllActive();
    }

    public SupplierApiConfig findBySupplierId(SupplierId supplierId) {
        return apiConfigQueryPort.findBySupplierId(supplierId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "공급자 API 설정이 없습니다: supplierId=" + supplierId.value()));
    }
}
