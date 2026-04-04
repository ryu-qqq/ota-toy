package com.ryuqq.otatoy.domain.common;

import java.util.Collections;
import java.util.Map;

public abstract class DomainException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, Object> args;

    protected DomainException(ErrorCode errorCode) {
        this(errorCode, Map.of());
    }

    protected DomainException(ErrorCode errorCode, Map<String, Object> args) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.args = args != null ? Collections.unmodifiableMap(args) : Map.of();
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Map<String, Object> getArgs() {
        return args;
    }
}
