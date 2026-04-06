package com.ryuqq.otatoy.application.supplier.service;

import com.ryuqq.otatoy.application.supplier.dto.FetchSupplierCommand;
import com.ryuqq.otatoy.application.supplier.dto.SupplierFetchResult;
import com.ryuqq.otatoy.application.supplier.factory.SupplierRawDataFactory;
import com.ryuqq.otatoy.application.supplier.manager.SupplierClientManager;
import com.ryuqq.otatoy.application.supplier.manager.SupplierRawDataCommandManager;
import com.ryuqq.otatoy.application.supplier.port.in.FetchSupplierUseCase;
import com.ryuqq.otatoy.domain.supplier.SupplierRawData;

import org.springframework.stereotype.Service;

/**
 * 공급자 데이터 수집 Service (1단계).
 * 외부 API를 호출하여 Raw 데이터를 DB에 저장한다.
 * UseCase 구현체로서 오케스트레이션만 담당한다 (APP-SVC-001).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Service
public class FetchSupplierService implements FetchSupplierUseCase {

    private final SupplierClientManager supplierClientManager;
    private final SupplierRawDataFactory rawDataFactory;
    private final SupplierRawDataCommandManager rawDataCommandManager;

    public FetchSupplierService(SupplierClientManager supplierClientManager,
                                 SupplierRawDataFactory rawDataFactory,
                                 SupplierRawDataCommandManager rawDataCommandManager) {
        this.supplierClientManager = supplierClientManager;
        this.rawDataFactory = rawDataFactory;
        this.rawDataCommandManager = rawDataCommandManager;
    }

    @Override
    public void execute(FetchSupplierCommand command) {
        // 1. 외부 API 호출
        SupplierFetchResult result = supplierClientManager.fetchProperties(command.supplierId());

        // 2. Raw 데이터 도메인 객체 생성
        SupplierRawData rawData = rawDataFactory.create(result);

        // 3. DB 저장 (FETCHED 상태)
        rawDataCommandManager.persist(rawData);
    }
}
