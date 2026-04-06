package com.ryuqq.otatoy.application.supplier.parser;

import com.ryuqq.otatoy.domain.supplier.SupplierApiType;
import com.ryuqq.otatoy.domain.supplier.SupplierTaskType;

import java.util.List;

/**
 * 공급자 RawData JSON 파싱 전략 인터페이스.
 * ApiType(공급자) + TaskType(수집 유형) 조합으로 구현체를 구분한다.
 *
 * @param <T> 파싱 결과 타입
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface SupplierRawDataParser<T> {

    SupplierApiType supportedApiType();

    SupplierTaskType supportedTaskType();

    List<T> parse(String rawPayload);
}
