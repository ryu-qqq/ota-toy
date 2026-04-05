package com.ryuqq.otatoy.domain.member;

import com.ryuqq.otatoy.domain.common.ErrorCategory;
import com.ryuqq.otatoy.domain.common.ErrorCode;

public enum MemberErrorCode implements ErrorCode {

    MEMBER_NOT_FOUND("MBR-001", "회원을 찾을 수 없습니다", ErrorCategory.NOT_FOUND),
    MEMBER_ALREADY_SUSPENDED("MBR-002", "이미 정지된 회원입니다", ErrorCategory.CONFLICT),
    MEMBER_ALREADY_ACTIVE("MBR-003", "이미 활성 상태인 회원입니다", ErrorCategory.CONFLICT),
    DUPLICATE_EMAIL("MBR-004", "이미 사용 중인 이메일입니다", ErrorCategory.CONFLICT);

    private final String code;
    private final String message;
    private final ErrorCategory category;

    MemberErrorCode(String code, String message, ErrorCategory category) {
        this.code = code;
        this.message = message;
        this.category = category;
    }

    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
    @Override public ErrorCategory getCategory() { return category; }
}
