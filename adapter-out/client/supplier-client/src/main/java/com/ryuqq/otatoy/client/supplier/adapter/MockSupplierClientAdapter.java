package com.ryuqq.otatoy.client.supplier.adapter;

import com.ryuqq.otatoy.application.supplier.dto.SupplierFetchResult;
import com.ryuqq.otatoy.application.supplier.strategy.SupplierStrategy;
import com.ryuqq.otatoy.client.supplier.exception.SupplierBadRequestException;
import com.ryuqq.otatoy.client.supplier.exception.SupplierNetworkException;
import com.ryuqq.otatoy.client.supplier.exception.SupplierServerException;
import com.ryuqq.otatoy.client.supplier.support.SupplierApiExecutor;
import com.ryuqq.otatoy.domain.supplier.SupplierApiConfig;
import com.ryuqq.otatoy.domain.supplier.SupplierApiType;

import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.Instant;

/**
 * Mock Supplier Client Adapter.
 * 실제 외부 API가 없으므로 Mock 데이터를 반환하되,
 * Circuit Breaker + Retry 구조와 에러 분류 체계는 실제와 동일하게 구현한다.
 *
 * 실제 Supplier가 추가되면 이 클래스를 참고하여 구현체를 만든다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class MockSupplierClientAdapter implements SupplierStrategy {

    private final SupplierApiExecutor apiExecutor;

    public MockSupplierClientAdapter(SupplierApiExecutor apiExecutor) {
        this.apiExecutor = apiExecutor;
    }

    @Override
    public SupplierApiType getApiType() {
        return SupplierApiType.MOCK;
    }

    @Override
    public SupplierFetchResult fetch(SupplierApiConfig config) {
        return apiExecutor.execute(() -> {
            try {
                // 실제 구현 시: RestClient로 config.baseUrl() + config.apiKey()로 호출
                // 지금은 Mock 데이터 반환
                String rawPayload = """
                    {
                        "properties": [
                            {
                                "externalId": "MOCK-001",
                                "name": "Mock Hotel Seoul",
                                "rooms": [
                                    {"externalId": "MOCK-R001", "name": "Standard Double"}
                                ]
                            }
                        ]
                    }
                    """;

                return new SupplierFetchResult(config.supplierId(), rawPayload, Instant.now());

            } catch (HttpServerErrorException e) {
                throw new SupplierServerException(e.getStatusCode().value(),
                        "Supplier API 서버 오류: " + e.getMessage(), e);
            } catch (HttpClientErrorException e) {
                throw new SupplierBadRequestException(e.getStatusCode().value(),
                        "Supplier API 요청 오류: " + e.getMessage());
            } catch (ResourceAccessException e) {
                throw new SupplierNetworkException(
                        "Supplier API 네트워크 오류: " + e.getMessage(), e);
            }
        });
    }

}
