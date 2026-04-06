package com.ryuqq.otatoy.client.supplier.config;

import com.ryuqq.otatoy.client.supplier.exception.SupplierBadRequestException;
import com.ryuqq.otatoy.client.supplier.exception.SupplierNetworkException;
import com.ryuqq.otatoy.client.supplier.exception.SupplierServerException;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Supplier 외부 API 호출용 Circuit Breaker + Retry 설정.
 * CrawlingHub EventBridge 패턴 기반.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Configuration
public class SupplierCircuitBreakerConfig {

    @Bean
    public CircuitBreaker supplierCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slowCallRateThreshold(80)
                .slowCallDurationThreshold(Duration.ofSeconds(3))
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(20)
                .minimumNumberOfCalls(10)
                .permittedNumberOfCallsInHalfOpenState(5)
                .waitDurationInOpenState(Duration.ofSeconds(60))
                .recordExceptions(SupplierServerException.class, SupplierNetworkException.class)
                .ignoreExceptions(SupplierBadRequestException.class)
                .build();

        return CircuitBreaker.of("supplier", config);
    }

    @Bean
    public Retry supplierRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .intervalFunction(attempt -> 100L * (long) Math.pow(2, attempt - 1))
                .retryExceptions(SupplierServerException.class, SupplierNetworkException.class)
                .ignoreExceptions(SupplierBadRequestException.class)
                .build();

        return Retry.of("supplier", config);
    }
}
