package com.ryuqq.otatoy.domain.partner;

import java.time.Instant;
import java.util.Objects;

public class PartnerMember {

    private final Long id;
    private final PartnerId partnerId;
    private String name;
    private String email;
    private String phone;
    private PartnerMemberRole role;
    private PartnerMemberStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private PartnerMember(Long id, PartnerId partnerId, String name, String email, String phone,
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

    public static PartnerMember forNew(PartnerId partnerId, String name, String email, String phone,
                                        PartnerMemberRole role, Instant now) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("멤버 이름은 필수입니다");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다");
        }
        if (role == null) {
            throw new IllegalArgumentException("역할은 필수입니다");
        }
        return new PartnerMember(null, partnerId, name, email, phone, role,
                PartnerMemberStatus.ACTIVE, now, now);
    }

    public static PartnerMember reconstitute(Long id, PartnerId partnerId, String name, String email, String phone,
                                              PartnerMemberRole role, PartnerMemberStatus status,
                                              Instant createdAt, Instant updatedAt) {
        return new PartnerMember(id, partnerId, name, email, phone, role, status, createdAt, updatedAt);
    }

    public Long id() { return id; }
    public PartnerId partnerId() { return partnerId; }
    public String name() { return name; }
    public String email() { return email; }
    public String phone() { return phone; }
    public PartnerMemberRole role() { return role; }
    public PartnerMemberStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PartnerMember m)) return false;
        return id != null && id.equals(m.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
