package com.ryuqq.otatoy.client.supplier.exception;

/**
 * 5xx 서버 오류. 재시도 대상.
 */
public class SupplierServerException extends SupplierClientException {

    public SupplierServerException(int statusCode, String message) {
        super(ErrorType.SERVER_ERROR, statusCode, message);
    }

    public SupplierServerException(int statusCode, String message, Throwable cause) {
        super(ErrorType.SERVER_ERROR, statusCode, message, cause);
    }
}
