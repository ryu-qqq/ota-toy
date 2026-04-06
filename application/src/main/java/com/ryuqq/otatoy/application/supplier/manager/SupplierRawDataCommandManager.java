package com.ryuqq.otatoy.application.supplier.manager;

import com.ryuqq.otatoy.application.supplier.port.out.SupplierRawDataCommandPort;
import com.ryuqq.otatoy.domain.supplier.SupplierRawData;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * SupplierRawData 저장 트랜잭션 경계 관리자.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierRawDataCommandManager {

    private final SupplierRawDataCommandPort rawDataCommandPort;

    public SupplierRawDataCommandManager(SupplierRawDataCommandPort rawDataCommandPort) {
        this.rawDataCommandPort = rawDataCommandPort;
    }

    @Transactional
    public Long persist(SupplierRawData rawData) {
        return rawDataCommandPort.persist(rawData);
    }
}
