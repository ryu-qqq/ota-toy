package com.ryuqq.otatoy.domain.common;

/**
 * 도메인 에러 코드 인터페이스.
 * 각 Bounded Context별 ErrorCode enum이 이 인터페이스를 구현한다.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public interface ErrorCode {

    String getCode();

    String getMessage();

    ErrorCategory getCategory();
}
