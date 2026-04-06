package com.ryuqq.otatoy.client.supplier.exception;

/**
 * 네트워크 오류 (타임아웃, 연결 실패 등). 재시도 대상.
 */
public class SupplierNetworkException extends SupplierClientException {

    public SupplierNetworkException(String message, Throwable cause) {
        super(ErrorType.NETWORK, 0, message, cause);
    }
}
