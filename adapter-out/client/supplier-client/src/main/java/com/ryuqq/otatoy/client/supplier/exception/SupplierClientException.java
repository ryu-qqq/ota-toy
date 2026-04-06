package com.ryuqq.otatoy.client.supplier.exception;

/**
 * Supplier 외부 API 호출 예외 기본 클래스.
 * ErrorType으로 재시도 가능 여부를 판단한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public class SupplierClientException extends RuntimeException {

    private final ErrorType errorType;
    private final int statusCode;

    public SupplierClientException(ErrorType errorType, int statusCode, String message) {
        super(message);
        this.errorType = errorType;
        this.statusCode = statusCode;
    }

    public SupplierClientException(ErrorType errorType, int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.statusCode = statusCode;
    }

    public ErrorType errorType() {
        return errorType;
    }

    public int statusCode() {
        return statusCode;
    }

    public boolean isRetryable() {
        return errorType.retryable;
    }

    public enum ErrorType {
        SERVER_ERROR(true),
        NETWORK(true),
        BAD_REQUEST(false),
        CIRCUIT_OPEN(false),
        UNKNOWN(false);

        final boolean retryable;

        ErrorType(boolean retryable) {
            this.retryable = retryable;
        }
    }
}
