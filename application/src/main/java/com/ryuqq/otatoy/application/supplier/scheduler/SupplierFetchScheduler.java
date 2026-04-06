package com.ryuqq.otatoy.application.supplier.scheduler;

import com.ryuqq.otatoy.application.supplier.dto.FetchSupplierCommand;
import com.ryuqq.otatoy.application.supplier.manager.SupplierReadManager;
import com.ryuqq.otatoy.application.supplier.port.in.FetchSupplierUseCase;
import com.ryuqq.otatoy.domain.supplier.Supplier;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 공급자 데이터 수집 스케줄러.
 * 5분 간격으로 활성 공급자의 외부 API를 호출하여 Raw 데이터를 수집한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierFetchScheduler {

    private final SupplierReadManager supplierReadManager;
    private final FetchSupplierUseCase fetchSupplierUseCase;

    public SupplierFetchScheduler(SupplierReadManager supplierReadManager,
                                   FetchSupplierUseCase fetchSupplierUseCase) {
        this.supplierReadManager = supplierReadManager;
        this.fetchSupplierUseCase = fetchSupplierUseCase;
    }

    @Scheduled(fixedDelay = 300000)
    public void fetch() {
        List<Supplier> activeSuppliers = supplierReadManager.findActiveSuppliers();

        for (Supplier supplier : activeSuppliers) {
            try {
                fetchSupplierUseCase.execute(new FetchSupplierCommand(supplier.id()));
            } catch (Exception e) {
                // 개별 공급자 실패는 전체 수집에 영향을 주지 않는다
            }
        }
    }
}
