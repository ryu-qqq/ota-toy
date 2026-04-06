package com.ryuqq.otatoy.application.supplier.processor;

import com.ryuqq.otatoy.domain.supplier.SupplierRawData;
import com.ryuqq.otatoy.domain.supplier.SupplierTaskType;

/**
 * 공급자 RawData 가공 전략 인터페이스.
 * TaskType별로 구현체를 제공하여 다른 가공 로직을 적용한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface SupplierRawDataProcessor {

    /**
     * 이 프로세서가 처리하는 TaskType을 반환한다.
     */
    SupplierTaskType supportedType();

    /**
     * RawData를 가공하여 자사 플랫폼 도메인에 통합한다.
     */
    void process(SupplierRawData rawData);
}
