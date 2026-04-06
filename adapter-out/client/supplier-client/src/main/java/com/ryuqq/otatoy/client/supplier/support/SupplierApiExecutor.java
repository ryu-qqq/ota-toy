package com.ryuqq.otatoy.client.supplier.support;

import com.ryuqq.otatoy.application.common.exception.ExternalServiceUnavailableException;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;

import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Supplier 외부 API 호출 실행기.
 * Circuit Breaker(바깥) + Retry(안쪽) 래핑.
 * CrawlingHub EventBridgeApiExecutor 패턴.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierApiExecutor {

    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public SupplierApiExecutor(CircuitBreaker supplierCircuitBreaker, Retry supplierRetry) {
        this.circuitBreaker = supplierCircuitBreaker;
        this.retry = supplierRetry;
    }

    /**
     * CB + Retry 보호 하에 외부 호출을 실행한다.
     * CB OPEN 시 ExternalServiceUnavailableException 발생 — Application 레이어가 인식하는 유일한 예외.
     */
    public <T> T execute(Supplier<T> supplier) {
        Supplier<T> retryDecorated = Retry.decorateSupplier(retry, supplier);
        try {
            return circuitBreaker.executeSupplier(retryDecorated);
        } catch (CallNotPermittedException e) {
            throw new ExternalServiceUnavailableException(
                    "Supplier Circuit Breaker OPEN", e);
        }
    }
}
