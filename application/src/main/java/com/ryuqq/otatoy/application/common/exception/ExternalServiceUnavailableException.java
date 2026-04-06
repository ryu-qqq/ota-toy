package com.ryuqq.otatoy.application.common.exception;

/**
 * 외부 서비스가 일시적으로 사용 불가능한 상태를 나타내는 예외.
 * Circuit Breaker OPEN 시 Adapter에서 이 예외를 던진다.
 *
 * Application 레이어에서 이 예외를 catch하면:
 * - 재시도 카운트를 소진하지 않고 작업을 지연시킨다 (deferRetry)
 * - 외부 서비스가 복구될 때까지 대기 후 재처리한다
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public class ExternalServiceUnavailableException extends RuntimeException {

    public ExternalServiceUnavailableException(String message) {
        super(message);
    }

    public ExternalServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
