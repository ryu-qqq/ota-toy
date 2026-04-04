package com.ryuqq.otatoy.domain.partner;

import com.ryuqq.otatoy.domain.common.vo.Email;
import com.ryuqq.otatoy.domain.common.vo.PhoneNumber;

import java.time.Instant;

/**
 * Partner BC 테스트용 Fixture.
 * 다양한 상태의 Partner, PartnerMember를 생성한다.
 */
public final class PartnerFixture {

    private PartnerFixture() {}

    // === 기본 상수 ===
    public static final Instant DEFAULT_NOW = Instant.parse("2026-04-04T00:00:00Z");
    public static final PartnerName DEFAULT_PARTNER_NAME = PartnerName.of("테스트파트너");
    public static final MemberName DEFAULT_MEMBER_NAME = MemberName.of("홍길동");
    public static final Email DEFAULT_EMAIL = Email.of("member@test.com");
    public static final PhoneNumber DEFAULT_PHONE = PhoneNumber.of("010-1234-5678");

    // === Partner Fixture ===

    /**
     * 신규 ACTIVE 상태 파트너
     */
    public static Partner activePartner() {
        return Partner.forNew(DEFAULT_PARTNER_NAME, DEFAULT_NOW);
    }

    /**
     * 지정 상태의 파트너 (reconstitute 사용)
     */
    public static Partner partnerWithStatus(PartnerStatus status) {
        return Partner.reconstitute(
                PartnerId.of(1L), DEFAULT_PARTNER_NAME, status, DEFAULT_NOW, DEFAULT_NOW
        );
    }

    /**
     * SUSPENDED 상태 파트너
     */
    public static Partner suspendedPartner() {
        return partnerWithStatus(PartnerStatus.SUSPENDED);
    }

    /**
     * DB 복원된 ACTIVE 파트너
     */
    public static Partner reconstitutedPartner() {
        return partnerWithStatus(PartnerStatus.ACTIVE);
    }

    /**
     * 지정 ID의 파트너
     */
    public static Partner partnerWithId(long id) {
        return Partner.reconstitute(
                PartnerId.of(id), DEFAULT_PARTNER_NAME, PartnerStatus.ACTIVE, DEFAULT_NOW, DEFAULT_NOW
        );
    }

    // === PartnerMember Fixture ===

    /**
     * 신규 ACTIVE 상태 멤버 (OWNER 역할)
     */
    public static PartnerMember activeOwnerMember() {
        return PartnerMember.forNew(
                PartnerId.of(1L), DEFAULT_MEMBER_NAME, DEFAULT_EMAIL, DEFAULT_PHONE,
                PartnerMemberRole.OWNER, DEFAULT_NOW
        );
    }

    /**
     * 신규 ACTIVE 상태 멤버 (STAFF 역할)
     */
    public static PartnerMember activeStaffMember() {
        return PartnerMember.forNew(
                PartnerId.of(1L), DEFAULT_MEMBER_NAME, DEFAULT_EMAIL, DEFAULT_PHONE,
                PartnerMemberRole.STAFF, DEFAULT_NOW
        );
    }

    /**
     * 지정 역할의 멤버
     */
    public static PartnerMember memberWithRole(PartnerMemberRole role) {
        return PartnerMember.forNew(
                PartnerId.of(1L), DEFAULT_MEMBER_NAME, DEFAULT_EMAIL, DEFAULT_PHONE,
                role, DEFAULT_NOW
        );
    }

    /**
     * 지정 상태의 멤버 (reconstitute 사용)
     */
    public static PartnerMember memberWithStatus(PartnerMemberStatus status) {
        return PartnerMember.reconstitute(
                PartnerMemberId.of(1L), PartnerId.of(1L), DEFAULT_MEMBER_NAME, DEFAULT_EMAIL, DEFAULT_PHONE,
                PartnerMemberRole.STAFF, status, DEFAULT_NOW, DEFAULT_NOW
        );
    }

    /**
     * SUSPENDED 상태 멤버
     */
    public static PartnerMember suspendedMember() {
        return memberWithStatus(PartnerMemberStatus.SUSPENDED);
    }

    /**
     * DB 복원된 ACTIVE 멤버
     */
    public static PartnerMember reconstitutedMember() {
        return memberWithStatus(PartnerMemberStatus.ACTIVE);
    }
}
