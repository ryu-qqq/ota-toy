package com.ryuqq.otatoy.client.supplier.support;

import com.ryuqq.otatoy.application.common.exception.ExternalServiceUnavailableException;
import com.ryuqq.otatoy.client.supplier.config.SupplierCircuitBreakerConfig;
import com.ryuqq.otatoy.client.supplier.exception.SupplierBadRequestException;
import com.ryuqq.otatoy.client.supplier.exception.SupplierNetworkException;
import com.ryuqq.otatoy.client.supplier.exception.SupplierServerException;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SupplierApiExecutor 단위 테스트.
 * Circuit Breaker + Retry 동작을 검증한다.
 */
class SupplierApiExecutorTest {

    private CircuitBreaker circuitBreaker;
    private Retry retry;
    private SupplierApiExecutor executor;

    @BeforeEach
    void setUp() {
        // 테스트용 CB: 최소 2건, 실패율 50%, slidingWindow 4
        circuitBreaker = CircuitBreaker.of("test", CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(4)
                .minimumNumberOfCalls(2)
                .waitDurationInOpenState(Duration.ofSeconds(60))
                .recordExceptions(SupplierServerException.class, SupplierNetworkException.class)
                .ignoreExceptions(SupplierBadRequestException.class)
                .build());

        // 테스트용 Retry: 최대 3회, 간격 없음
        retry = Retry.of("test", RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ZERO)
                .retryExceptions(SupplierServerException.class, SupplierNetworkException.class)
                .ignoreExceptions(SupplierBadRequestException.class)
                .build());

        executor = new SupplierApiExecutor(circuitBreaker, retry);
    }

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("정상 호출 시 결과를 반환한다")
        void shouldReturnResultOnSuccess() {
            String result = executor.execute(() -> "OK");
            assertThat(result).isEqualTo("OK");
        }
    }

    @Nested
    @DisplayName("Retry 동작")
    class RetryBehavior {

        @Test
        @DisplayName("ServerException 발생 시 3회 재시도 후 예외를 전파한다")
        void shouldRetry3TimesOnServerException() {
            AtomicInteger callCount = new AtomicInteger(0);

            assertThatThrownBy(() -> executor.execute(() -> {
                callCount.incrementAndGet();
                throw new SupplierServerException(500, "Internal Server Error");
            })).isInstanceOf(SupplierServerException.class);

            assertThat(callCount.get()).isEqualTo(3);
        }

        @Test
        @DisplayName("NetworkException 발생 시 3회 재시도 후 예외를 전파한다")
        void shouldRetry3TimesOnNetworkException() {
            AtomicInteger callCount = new AtomicInteger(0);

            assertThatThrownBy(() -> executor.execute(() -> {
                callCount.incrementAndGet();
                throw new SupplierNetworkException("Connection refused", new RuntimeException());
            })).isInstanceOf(SupplierNetworkException.class);

            assertThat(callCount.get()).isEqualTo(3);
        }

        @Test
        @DisplayName("BadRequestException은 재시도하지 않고 즉시 전파한다")
        void shouldNotRetryOnBadRequestException() {
            AtomicInteger callCount = new AtomicInteger(0);

            assertThatThrownBy(() -> executor.execute(() -> {
                callCount.incrementAndGet();
                throw new SupplierBadRequestException(400, "Bad Request");
            })).isInstanceOf(SupplierBadRequestException.class);

            assertThat(callCount.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("첫 시도 실패 후 두 번째에 성공하면 결과를 반환한다")
        void shouldSucceedOnSecondAttempt() {
            AtomicInteger callCount = new AtomicInteger(0);

            String result = executor.execute(() -> {
                if (callCount.incrementAndGet() == 1) {
                    throw new SupplierServerException(500, "Temporary failure");
                }
                return "OK";
            });

            assertThat(result).isEqualTo("OK");
            assertThat(callCount.get()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Circuit Breaker 동작")
    class CircuitBreakerBehavior {

        @Test
        @DisplayName("실패율 초과 시 CB가 OPEN되어 ExternalServiceUnavailableException을 던진다")
        void shouldThrowExternalServiceUnavailableWhenCBOpen() {
            // Retry 없이 CB만 사용하는 executor로 테스트 (Retry가 CB 기록 횟수에 영향을 주지 않도록)
            Retry noRetry = Retry.of("no-retry", RetryConfig.custom()
                    .maxAttempts(1).build());
            SupplierApiExecutor cbOnlyExecutor = new SupplierApiExecutor(circuitBreaker, noRetry);

            // CB를 열기 위해 실패를 충분히 발생시킨다 (minimumNumberOfCalls=2, slidingWindow=4)
            for (int i = 0; i < 4; i++) {
                try {
                    cbOnlyExecutor.execute(() -> {
                        throw new SupplierServerException(500, "fail");
                    });
                } catch (Exception ignored) {
                }
            }

            assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

            assertThatThrownBy(() -> cbOnlyExecutor.execute(() -> "should not reach"))
                    .isInstanceOf(ExternalServiceUnavailableException.class)
                    .hasMessageContaining("Circuit Breaker OPEN");
        }

        @Test
        @DisplayName("BadRequestException은 CB에 기록되지 않는다")
        void shouldNotRecordBadRequestInCB() {
            // BadRequest를 여러 번 발생시켜도 CB가 열리지 않아야 함
            for (int i = 0; i < 10; i++) {
                try {
                    executor.execute(() -> {
                        throw new SupplierBadRequestException(400, "bad");
                    });
                } catch (SupplierBadRequestException ignored) {
                }
            }

            assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        }

        @Test
        @DisplayName("성공 호출 후 실패율이 임계값 이하면 CB는 CLOSED 상태를 유지한다")
        void shouldRemainClosedWhenFailureRateBelowThreshold() {
            // 성공 3 + 실패 1 = 실패율 25% (임계값 50% 미만)
            executor.execute(() -> "ok");
            executor.execute(() -> "ok");
            executor.execute(() -> "ok");

            try {
                executor.execute(() -> {
                    throw new SupplierServerException(500, "fail");
                });
            } catch (SupplierServerException ignored) {
            }

            assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        }
    }

    @Nested
    @DisplayName("예외 분류 검증")
    class ExceptionClassification {

        @Test
        @DisplayName("ServerException의 ErrorType은 SERVER_ERROR이고 retryable이다")
        void serverExceptionShouldBeRetryable() {
            SupplierServerException ex = new SupplierServerException(500, "error");
            assertThat(ex.isRetryable()).isTrue();
            assertThat(ex.statusCode()).isEqualTo(500);
        }

        @Test
        @DisplayName("NetworkException의 ErrorType은 NETWORK이고 retryable이다")
        void networkExceptionShouldBeRetryable() {
            SupplierNetworkException ex = new SupplierNetworkException("timeout", new RuntimeException());
            assertThat(ex.isRetryable()).isTrue();
            assertThat(ex.statusCode()).isEqualTo(0);
        }

        @Test
        @DisplayName("BadRequestException의 ErrorType은 BAD_REQUEST이고 retryable이 아니다")
        void badRequestExceptionShouldNotBeRetryable() {
            SupplierBadRequestException ex = new SupplierBadRequestException(400, "bad");
            assertThat(ex.isRetryable()).isFalse();
            assertThat(ex.statusCode()).isEqualTo(400);
        }
    }
}
