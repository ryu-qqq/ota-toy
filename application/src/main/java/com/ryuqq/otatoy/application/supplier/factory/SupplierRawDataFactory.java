package com.ryuqq.otatoy.application.supplier.factory;

import com.ryuqq.otatoy.application.common.factory.TimeProvider;
import com.ryuqq.otatoy.application.supplier.dto.SupplierFetchResult;
import com.ryuqq.otatoy.domain.supplier.SupplierRawData;

import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * SupplierRawData 도메인 객체 생성 팩토리.
 * TimeProvider를 주입받아 시간을 일원화한다 (APP-FAC-001).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierRawDataFactory {

    private final TimeProvider timeProvider;

    public SupplierRawDataFactory(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    /**
     * SupplierFetchResult로부터 SupplierRawData 도메인 객체를 생성한다.
     */
    public SupplierRawData create(SupplierFetchResult result) {
        Instant now = timeProvider.now();
        return SupplierRawData.forNew(result.supplierId(), result.rawPayload(), now);
    }
}
