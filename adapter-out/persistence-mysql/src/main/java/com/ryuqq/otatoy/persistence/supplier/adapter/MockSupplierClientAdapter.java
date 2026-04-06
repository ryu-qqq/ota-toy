package com.ryuqq.otatoy.persistence.supplier.adapter;

import com.ryuqq.otatoy.application.supplier.dto.SupplierFetchResult;
import com.ryuqq.otatoy.application.supplier.port.out.SupplierClient;
import com.ryuqq.otatoy.domain.supplier.SupplierId;
import org.springframework.stereotype.Component;

/**
 * Mock 공급자 API 클라이언트.
 * 외부 API 연동 전 테스트용으로 사용하는 Mock 구현체.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class MockSupplierClientAdapter implements SupplierClient {

    @Override
    public SupplierFetchResult fetchProperties(SupplierId supplierId) {
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
                    },
                    {
                        "externalPropertyId": "EXT-002",
                        "name": "부산 해운대 리조트",
                        "description": "바다가 보이는 리조트",
                        "address": "부산시 해운대구 해운대로 200",
                        "latitude": 35.1587,
                        "longitude": 129.1604,
                        "propertyType": "RESORT",
                        "rooms": [
                            {"externalRoomId": "R-003", "name": "오션뷰 더블", "maxOccupancy": 2, "price": 180000}
                        ]
                    }
                ]
                """;
        return new SupplierFetchResult(supplierId, mockJson);
    }
}
