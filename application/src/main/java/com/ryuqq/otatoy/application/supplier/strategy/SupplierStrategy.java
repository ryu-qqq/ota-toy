package com.ryuqq.otatoy.application.supplier.strategy;

import com.ryuqq.otatoy.application.supplier.dto.SupplierFetchResult;
import com.ryuqq.otatoy.domain.supplier.SupplierApiConfig;
import com.ryuqq.otatoy.domain.supplier.SupplierApiType;

/**
 * 공급자별 데이터 수집 전략 인터페이스.
 * 외부 API 호출만 담당한다. 파싱은 SupplierRawDataParser가 담당한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface SupplierStrategy {

    SupplierApiType getApiType();

    SupplierFetchResult fetch(SupplierApiConfig config);
}
