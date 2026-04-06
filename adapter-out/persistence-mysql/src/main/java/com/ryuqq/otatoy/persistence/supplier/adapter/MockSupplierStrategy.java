package com.ryuqq.otatoy.persistence.supplier.adapter;

import com.ryuqq.otatoy.application.supplier.dto.SupplierFetchResult;
import com.ryuqq.otatoy.application.supplier.strategy.SupplierStrategy;
import com.ryuqq.otatoy.domain.supplier.SupplierApiConfig;
import com.ryuqq.otatoy.domain.supplier.SupplierApiType;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Mock 공급자 수집 전략.
 * 외부 API 연동 전 테스트용. 파싱은 SupplierRawDataParser가 담당한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class MockSupplierStrategy implements SupplierStrategy {

    @Override
    public SupplierApiType getApiType() {
        return SupplierApiType.MOCK;
    }

    @Override
    public SupplierFetchResult fetch(SupplierApiConfig config) {
        String mockJson = """
                [
                    {
                        "externalPropertyId": "EXT-001",
                        "name": "서울 그랜드 호텔",
                        "description": "도심 속 5성급 호텔",
                        "address": "서울시 중구 을지로 100",
                        "latitude": 37.5660,
                        "longitude": 126.9784,
                        "propertyType": "HOTEL",
                        "rooms": [
                            {"externalRoomId": "R-001", "name": "디럭스 더블", "maxOccupancy": 2, "price": 200000},
                            {"externalRoomId": "R-002", "name": "스위트", "maxOccupancy": 4, "price": 450000}
                        ]
                    }
                ]
                """;
        return new SupplierFetchResult(config.supplierId(), mockJson, Instant.now());
    }
}
