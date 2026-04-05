package com.ryuqq.otatoy.domain.member;

/**
 * 회원을 찾을 수 없을 때 발생하는 예외.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public class MemberNotFoundException extends MemberException {

    public MemberNotFoundException() {
        super(MemberErrorCode.MEMBER_NOT_FOUND);
    }
}
