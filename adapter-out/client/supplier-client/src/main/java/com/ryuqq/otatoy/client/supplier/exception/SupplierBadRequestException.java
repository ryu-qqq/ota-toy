package com.ryuqq.otatoy.client.supplier.exception;

/**
 * 4xx 클라이언트 오류. 재시도 불가 (요청 자체가 잘못됨).
 */
public class SupplierBadRequestException extends SupplierClientException {

    public SupplierBadRequestException(int statusCode, String message) {
        super(ErrorType.BAD_REQUEST, statusCode, message);
    }
}
