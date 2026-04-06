package com.ryuqq.otatoy.application.supplier.manager;

import com.ryuqq.otatoy.application.supplier.port.out.SupplierRawDataCommandPort;
import com.ryuqq.otatoy.domain.supplier.SupplierRawData;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * SupplierRawData 쓰기 트랜잭션 관리자.
 * 상태 전이는 도메인 객체가 담당하고, Manager는 persist만 한다 (APP-MGR-001).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierRawDataTransactionManager {

    private final SupplierRawDataCommandPort rawDataCommandPort;

    public SupplierRawDataTransactionManager(SupplierRawDataCommandPort rawDataCommandPort) {
        this.rawDataCommandPort = rawDataCommandPort;
    }

    @Transactional
    public void persist(SupplierRawData rawData) {
        rawDataCommandPort.persist(rawData);
    }
}
