package com.ryuqq.otatoy.domain.member;

/**
 * 회원 역할. 인가(Authorization) 판단에 사용한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public enum MemberRole {

    CUSTOMER("고객"),
    ADMIN("관리자");

    private final String displayName;

    MemberRole(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
