package com.ryuqq.otatoy.domain.member;

import com.ryuqq.otatoy.domain.common.DomainException;
import com.ryuqq.otatoy.domain.common.ErrorCode;

/**
 * 회원 도메인 예외 기본 클래스.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public class MemberException extends DomainException {

    public MemberException(ErrorCode errorCode) {
        super(errorCode);
    }
}
