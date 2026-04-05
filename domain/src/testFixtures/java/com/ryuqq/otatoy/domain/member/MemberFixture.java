package com.ryuqq.otatoy.domain.member;

import java.time.Instant;

/**
 * Member BC 테스트용 Fixture.
 */
public final class MemberFixture {

    private MemberFixture() {}

    public static final Instant DEFAULT_NOW = Instant.parse("2026-04-04T00:00:00Z");
    public static final MemberEmail DEFAULT_EMAIL = MemberEmail.of("user@test.com");
    public static final MemberPassword DEFAULT_PASSWORD = MemberPassword.of("$2a$10$hashedPasswordValue");
    public static final MemberName DEFAULT_NAME = MemberName.of("홍길동");

    public static Member activeMember() {
        return Member.forNew(DEFAULT_EMAIL, DEFAULT_PASSWORD, DEFAULT_NAME, DEFAULT_NOW);
    }

    public static Member memberWithStatus(MemberStatus status) {
        return Member.reconstitute(
                MemberId.of(1L), DEFAULT_EMAIL, DEFAULT_PASSWORD, DEFAULT_NAME,
                MemberRole.CUSTOMER, status, DEFAULT_NOW, DEFAULT_NOW
        );
    }

    public static Member adminMember() {
        return Member.reconstitute(
                MemberId.of(99L), MemberEmail.of("admin@test.com"), DEFAULT_PASSWORD,
                MemberName.of("관리자"), MemberRole.ADMIN, MemberStatus.ACTIVE,
                DEFAULT_NOW, DEFAULT_NOW
        );
    }

    public static Member suspendedMember() {
        return memberWithStatus(MemberStatus.SUSPENDED);
    }

    public static Member reconstitutedMember(long id) {
        return Member.reconstitute(
                MemberId.of(id), DEFAULT_EMAIL, DEFAULT_PASSWORD, DEFAULT_NAME,
                MemberRole.CUSTOMER, MemberStatus.ACTIVE, DEFAULT_NOW, DEFAULT_NOW
        );
    }
}
