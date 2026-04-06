package com.ryuqq.otatoy.application.supplier.scheduler;

import com.ryuqq.otatoy.application.supplier.dto.SyncSupplierCommand;
import com.ryuqq.otatoy.application.supplier.manager.SupplierReadManager;
import com.ryuqq.otatoy.application.supplier.port.in.SyncSupplierUseCase;
import com.ryuqq.otatoy.domain.supplier.Supplier;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 공급자 데이터 가공/동기화 스케줄러.
 * 1분 간격으로 FETCHED 상태의 Raw 데이터를 가공하여 동기화한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierSyncScheduler {

    private final SupplierReadManager supplierReadManager;
    private final SyncSupplierUseCase syncSupplierUseCase;

    public SupplierSyncScheduler(SupplierReadManager supplierReadManager,
                                  SyncSupplierUseCase syncSupplierUseCase) {
        this.supplierReadManager = supplierReadManager;
        this.syncSupplierUseCase = syncSupplierUseCase;
    }

    @Scheduled(fixedDelay = 60000)
    public void sync() {
        List<Supplier> activeSuppliers = supplierReadManager.findActiveSuppliers();

        for (Supplier supplier : activeSuppliers) {
            try {
                syncSupplierUseCase.execute(new SyncSupplierCommand(supplier.id()));
            } catch (Exception e) {
                // 개별 공급자 실패는 전체 동기화에 영향을 주지 않는다
            }
        }
    }
}
