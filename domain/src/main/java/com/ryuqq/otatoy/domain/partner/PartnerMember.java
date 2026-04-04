package com.ryuqq.otatoy.domain.partner;

import com.ryuqq.otatoy.domain.common.vo.Email;
import com.ryuqq.otatoy.domain.common.vo.PhoneNumber;

import java.time.Instant;
import java.util.Objects;

/**
 * 파트너에 소속된 멤버를 나타내는 엔티티.
 * 소유자, 관리자, 직원 역할을 가지며, 이메일/전화번호를 포함한다.
 */
public class PartnerMember {

    private final PartnerMemberId id;
    private final PartnerId partnerId;
    private MemberName name;
    private Email email;
    private PhoneNumber phone;
    private PartnerMemberRole role;
    private PartnerMemberStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private PartnerMember(PartnerMemberId id, PartnerId partnerId, MemberName name, Email email, PhoneNumber phone,
                          PartnerMemberRole role, PartnerMemberStatus status,
                          Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.partnerId = partnerId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PartnerMember forNew(PartnerId partnerId, MemberName name, Email email, PhoneNumber phone,
                                        PartnerMemberRole role, Instant now) {
        validate(partnerId, role);
        return new PartnerMember(PartnerMemberId.of(null), partnerId, name, email, phone, role,
                PartnerMemberStatus.ACTIVE, now, now);
    }

    private static void validate(PartnerId partnerId, PartnerMemberRole role) {
        if (partnerId == null) {
            throw new IllegalArgumentException("파트너 ID는 필수입니다");
        }
        if (role == null) {
            throw new IllegalArgumentException("역할은 필수입니다");
        }
    }

    public static PartnerMember reconstitute(PartnerMemberId id, PartnerId partnerId, MemberName name, Email email, PhoneNumber phone,
                                              PartnerMemberRole role, PartnerMemberStatus status,
                                              Instant createdAt, Instant updatedAt) {
        return new PartnerMember(id, partnerId, name, email, phone, role, status, createdAt, updatedAt);
    }

    public void suspend(Instant now) {
        if (this.status == PartnerMemberStatus.SUSPENDED) {
            throw new IllegalStateException("이미 정지된 멤버입니다");
        }
        this.status = PartnerMemberStatus.SUSPENDED;
        this.updatedAt = now;
    }

    public void activate(Instant now) {
        if (this.status == PartnerMemberStatus.ACTIVE) {
            throw new IllegalStateException("이미 활성 상태인 멤버입니다");
        }
        this.status = PartnerMemberStatus.ACTIVE;
        this.updatedAt = now;
    }

    public void changeRole(PartnerMemberRole newRole, Instant now) {
        if (newRole == null) {
            throw new IllegalArgumentException("역할은 필수입니다");
        }
        this.role = newRole;
        this.updatedAt = now;
    }

    public void updateProfile(MemberName name, Email email, PhoneNumber phone, Instant now) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.updatedAt = now;
    }

    public boolean isActive() {
        return this.status == PartnerMemberStatus.ACTIVE;
    }

    public PartnerMemberId id() { return id; }
    public PartnerId partnerId() { return partnerId; }
    public MemberName name() { return name; }
    public Email email() { return email; }
    public PhoneNumber phone() { return phone; }
    public PartnerMemberRole role() { return role; }
    public PartnerMemberStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PartnerMember m)) return false;
        return id != null && !id.isNew() && id.equals(m.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hashCode(id) : System.identityHashCode(this);
    }
}
