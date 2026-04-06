package com.ryuqq.otatoy.application.supplier.service;

import com.ryuqq.otatoy.application.common.factory.TimeProvider;
import com.ryuqq.otatoy.application.supplier.dto.SupplierPropertyData;
import com.ryuqq.otatoy.application.supplier.dto.SupplierSyncDiff;
import com.ryuqq.otatoy.application.supplier.dto.SyncSupplierCommand;
import com.ryuqq.otatoy.application.supplier.facade.SupplierSyncPersistenceFacade;
import com.ryuqq.otatoy.application.supplier.manager.SupplierPropertyReadManager;
import com.ryuqq.otatoy.application.supplier.manager.SupplierRawDataCommandManager;
import com.ryuqq.otatoy.application.supplier.manager.SupplierRawDataReadManager;
import com.ryuqq.otatoy.application.supplier.port.in.SyncSupplierUseCase;
import com.ryuqq.otatoy.application.supplier.translator.SupplierTranslator;
import com.ryuqq.otatoy.domain.supplier.SupplierProperty;
import com.ryuqq.otatoy.domain.supplier.SupplierRawData;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * 공급자 데이터 가공/동기화 Service (2단계).
 * Raw 데이터를 파싱하여 Diff 계산 후 Property/매핑을 동기화한다.
 * UseCase 구현체로서 오케스트레이션만 담당한다 (APP-SVC-001).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Service
public class SyncSupplierService implements SyncSupplierUseCase {

    private final SupplierRawDataReadManager rawDataReadManager;
    private final SupplierRawDataCommandManager rawDataCommandManager;
    private final SupplierTranslator translator;
    private final SupplierPropertyReadManager supplierPropertyReadManager;
    private final SupplierSyncPersistenceFacade syncFacade;
    private final TimeProvider timeProvider;

    public SyncSupplierService(SupplierRawDataReadManager rawDataReadManager,
                                SupplierRawDataCommandManager rawDataCommandManager,
                                SupplierTranslator translator,
                                SupplierPropertyReadManager supplierPropertyReadManager,
                                SupplierSyncPersistenceFacade syncFacade,
                                TimeProvider timeProvider) {
        this.rawDataReadManager = rawDataReadManager;
        this.rawDataCommandManager = rawDataCommandManager;
        this.translator = translator;
        this.supplierPropertyReadManager = supplierPropertyReadManager;
        this.syncFacade = syncFacade;
        this.timeProvider = timeProvider;
    }

    @Override
    public void execute(SyncSupplierCommand command) {
        // 1. FETCHED 상태의 Raw 데이터 조회
        List<SupplierRawData> rawDataList = rawDataReadManager.findFetched(command.supplierId());

        for (SupplierRawData rawData : rawDataList) {
            // 2. 가공 시작 상태 전이
            rawData.markProcessing();

            try {
                // 3. Raw JSON → SupplierPropertyData 파싱
                List<SupplierPropertyData> properties = translator.parse(rawData.rawPayload());

                // 4. 기존 매핑 조회
                List<SupplierProperty> existingMappings =
                        supplierPropertyReadManager.findBySupplierId(command.supplierId());

                // 5. Diff 계산
                Instant now = timeProvider.now();
                SupplierSyncDiff diff = translator.calculateDiff(properties, existingMappings, now);

                // 6. 동기화 저장 (Property 생성/수정 + 매핑 저장 + SyncLog)
                syncFacade.sync(rawData, diff, command.supplierId());

            } catch (Exception e) {
                rawData.markFailed(timeProvider.now());
                rawDataCommandManager.persist(rawData);
            }
        }
    }
}
