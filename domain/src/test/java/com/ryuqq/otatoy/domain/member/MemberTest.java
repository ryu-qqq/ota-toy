package com.ryuqq.otatoy.domain.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberTest {

    private static final Instant NOW = Instant.parse("2026-04-04T00:00:00Z");
    private static final MemberEmail EMAIL = MemberEmail.of("user@test.com");
    private static final MemberPassword PASSWORD = MemberPassword.of("$2a$10$hashedValue");
    private static final MemberName NAME = MemberName.of("홍길동");

    @Nested
    @DisplayName("T-1: 생성 검증 — forNew()")
    class Creation {

        @Test
        @DisplayName("정상 생성 시 CUSTOMER 역할, ACTIVE 상태, id는 null")
        void shouldCreateWithDefaultRoleAndStatus() {
            Member member = Member.forNew(EMAIL, PASSWORD, NAME, NOW);

            assertThat(member.id()).isNull();
            assertThat(member.email()).isEqualTo(EMAIL);
            assertThat(member.password()).isEqualTo(PASSWORD);
            assertThat(member.name()).isEqualTo(NAME);
            assertThat(member.role()).isEqualTo(MemberRole.CUSTOMER);
            assertThat(member.status()).isEqualTo(MemberStatus.ACTIVE);
            assertThat(member.isActive()).isTrue();
            assertThat(member.isAdmin()).isFalse();
        }

        @Test
        @DisplayName("email이 null이면 생성 실패")
        void shouldFailWhenEmailIsNull() {
            assertThatThrownBy(() -> Member.forNew(null, PASSWORD, NAME, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이메일은 필수");
        }

        @Test
        @DisplayName("password가 null이면 생성 실패")
        void shouldFailWhenPasswordIsNull() {
            assertThatThrownBy(() -> Member.forNew(EMAIL, null, NAME, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("비밀번호는 필수");
        }

        @Test
        @DisplayName("name이 null이면 생성 실패")
        void shouldFailWhenNameIsNull() {
            assertThatThrownBy(() -> Member.forNew(EMAIL, PASSWORD, null, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이름은 필수");
        }
    }

    @Nested
    @DisplayName("T-2: 상태 전이")
    class StateTransition {

        @Test
        @DisplayName("ACTIVE → SUSPENDED 전이 성공")
        void shouldSuspend() {
            Member member = Member.forNew(EMAIL, PASSWORD, NAME, NOW);
            Instant suspendTime = Instant.parse("2026-04-05T00:00:00Z");

            member.suspend(suspendTime);

            assertThat(member.status()).isEqualTo(MemberStatus.SUSPENDED);
            assertThat(member.isActive()).isFalse();
            assertThat(member.updatedAt()).isEqualTo(suspendTime);
        }

        @Test
        @DisplayName("SUSPENDED → ACTIVE 전이 성공")
        void shouldActivate() {
            Member member = MemberFixture.suspendedMember();
            Instant activateTime = Instant.parse("2026-04-06T00:00:00Z");

            member.activate(activateTime);

            assertThat(member.status()).isEqualTo(MemberStatus.ACTIVE);
            assertThat(member.isActive()).isTrue();
        }

        @Test
        @DisplayName("ACTIVE에서 activate() 호출 시 예외")
        void shouldFailWhenAlreadyActive() {
            Member member = Member.forNew(EMAIL, PASSWORD, NAME, NOW);

            assertThatThrownBy(() -> member.activate(NOW))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("SUSPENDED에서 suspend() 호출 시 예외")
        void shouldFailWhenAlreadySuspended() {
            Member member = MemberFixture.suspendedMember();

            assertThatThrownBy(() -> member.suspend(NOW))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("T-3: 비밀번호 변경")
    class ChangePassword {

        @Test
        @DisplayName("새 비밀번호로 변경 성공")
        void shouldChangePassword() {
            Member member = Member.forNew(EMAIL, PASSWORD, NAME, NOW);
            MemberPassword newPassword = MemberPassword.of("$2a$10$newHashedValue");
            Instant changeTime = Instant.parse("2026-04-05T00:00:00Z");

            member.changePassword(newPassword, changeTime);

            assertThat(member.password()).isEqualTo(newPassword);
            assertThat(member.updatedAt()).isEqualTo(changeTime);
        }

        @Test
        @DisplayName("null 비밀번호면 예외")
        void shouldFailWhenNull() {
            Member member = Member.forNew(EMAIL, PASSWORD, NAME, NOW);

            assertThatThrownBy(() -> member.changePassword(null, NOW))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("T-4: 이름 변경")
    class ChangeName {

        @Test
        @DisplayName("새 이름으로 변경 성공")
        void shouldChangeName() {
            Member member = Member.forNew(EMAIL, PASSWORD, NAME, NOW);
            MemberName newName = MemberName.of("김철수");

            member.changeName(newName, NOW);

            assertThat(member.name()).isEqualTo(newName);
        }
    }

    @Nested
    @DisplayName("T-5: reconstitute")
    class Reconstitute {

        @Test
        @DisplayName("모든 필드가 그대로 복원된다")
        void shouldReconstitute() {
            Member member = Member.reconstitute(
                    MemberId.of(42L), EMAIL, PASSWORD, NAME,
                    MemberRole.ADMIN, MemberStatus.SUSPENDED, NOW, NOW
            );

            assertThat(member.id()).isEqualTo(MemberId.of(42L));
            assertThat(member.role()).isEqualTo(MemberRole.ADMIN);
            assertThat(member.status()).isEqualTo(MemberStatus.SUSPENDED);
            assertThat(member.isAdmin()).isTrue();
        }
    }

    @Nested
    @DisplayName("T-6: 동등성")
    class Equality {

        @Test
        @DisplayName("같은 ID면 동등하다")
        void shouldBeEqualWithSameId() {
            Member m1 = MemberFixture.reconstitutedMember(1L);
            Member m2 = Member.reconstitute(
                    MemberId.of(1L), MemberEmail.of("other@test.com"), PASSWORD,
                    MemberName.of("다른사람"), MemberRole.ADMIN, MemberStatus.SUSPENDED, NOW, NOW
            );

            assertThat(m1).isEqualTo(m2);
            assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
        }

        @Test
        @DisplayName("id가 null이면 동등하지 않다")
        void shouldNotBeEqualWhenIdIsNull() {
            Member m1 = Member.forNew(EMAIL, PASSWORD, NAME, NOW);
            Member m2 = Member.forNew(EMAIL, PASSWORD, NAME, NOW);

            assertThat(m1).isNotEqualTo(m2);
        }
    }
}
