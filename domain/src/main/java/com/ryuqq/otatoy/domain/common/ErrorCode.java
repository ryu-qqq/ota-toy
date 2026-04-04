package com.ryuqq.otatoy.domain.common;

public interface ErrorCode {

    String getCode();

    int getHttpStatus();

    String getMessage();
}
