package com.ryuqq.otatoy.domain.common;

import java.util.Collections;
import java.util.Map;

/**
 * 도메인 레이어의 기본 예외 클래스.
 * 모든 도메인 예외는 이 클래스를 상속하며, ErrorCode와 부가 인자를 포함한다.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
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
