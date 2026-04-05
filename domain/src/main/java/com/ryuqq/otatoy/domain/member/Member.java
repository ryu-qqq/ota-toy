package com.ryuqq.otatoy.domain.member;

import java.time.Instant;
import java.util.Objects;

/**
 * 회원을 나타내는 Aggregate Root.
 * 이메일로 로그인하며, 역할(CUSTOMER/ADMIN)로 인가를 구분한다.
 * 비밀번호 해싱은 도메인 외부(Application/Adapter)에서 수행하며,
 * 도메인은 해시된 값만 보관한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public class Member {

    private final MemberId id;
    private final MemberEmail email;
    private MemberPassword password;
    private MemberName name;
    private MemberRole role;
    private MemberStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private Member(MemberId id, MemberEmail email, MemberPassword password,
                   MemberName name, MemberRole role, MemberStatus status,
                   Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 회원 생성. 기본 역할은 CUSTOMER, 상태는 ACTIVE.
     * @param hashedPassword Application 레이어에서 해싱한 비밀번호
     */
    public static Member forNew(MemberEmail email, MemberPassword hashedPassword,
                                 MemberName name, Instant now) {
        validateRequired(email, hashedPassword, name);
        return new Member(null, email, hashedPassword, name,
                MemberRole.CUSTOMER, MemberStatus.ACTIVE, now, now);
    }

    public static Member reconstitute(MemberId id, MemberEmail email, MemberPassword password,
                                       MemberName name, MemberRole role, MemberStatus status,
                                       Instant createdAt, Instant updatedAt) {
        return new Member(id, email, password, name, role, status, createdAt, updatedAt);
    }

    private static void validateRequired(MemberEmail email, MemberPassword password, MemberName name) {
        if (email == null) {
            throw new IllegalArgumentException("이메일은 필수입니다");
        }
        if (password == null) {
            throw new IllegalArgumentException("비밀번호는 필수입니다");
        }
        if (name == null) {
            throw new IllegalArgumentException("이름은 필수입니다");
        }
    }

    public void changePassword(MemberPassword newHashedPassword, Instant now) {
        if (newHashedPassword == null) {
            throw new IllegalArgumentException("새 비밀번호는 필수입니다");
        }
        this.password = newHashedPassword;
        this.updatedAt = now;
    }

    public void changeName(MemberName newName, Instant now) {
        if (newName == null) {
            throw new IllegalArgumentException("새 이름은 필수입니다");
        }
        this.name = newName;
        this.updatedAt = now;
    }

    public void suspend(Instant now) {
        this.status = status.transitTo(MemberStatus.SUSPENDED);
        this.updatedAt = now;
    }

    public void activate(Instant now) {
        this.status = status.transitTo(MemberStatus.ACTIVE);
        this.updatedAt = now;
    }

    public boolean isActive() {
        return status == MemberStatus.ACTIVE;
    }

    public boolean isAdmin() {
        return role == MemberRole.ADMIN;
    }

    public MemberId id() { return id; }
    public MemberEmail email() { return email; }
    public MemberPassword password() { return password; }
    public MemberName name() { return name; }
    public MemberRole role() { return role; }
    public MemberStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member m)) return false;
        return id != null && id.equals(m.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
