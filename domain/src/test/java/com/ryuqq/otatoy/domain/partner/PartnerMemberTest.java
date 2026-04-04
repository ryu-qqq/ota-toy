package com.ryuqq.otatoy.domain.partner;

import com.ryuqq.otatoy.domain.common.vo.Email;
import com.ryuqq.otatoy.domain.common.vo.PhoneNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PartnerMemberTest {

    private static final Instant NOW = PartnerFixture.DEFAULT_NOW;
    private static final Instant LATER = NOW.plusSeconds(3600);

    @Nested
    @DisplayName("T-1: forNew() 팩토리 메서드")
    class ForNewTest {

        @Test
        @DisplayName("신규 멤버는 ACTIVE 상태로 생성된다")
        void shouldCreateActiveMember() {
            PartnerMember member = PartnerFixture.activeOwnerMember();

            assertThat(member.status()).isEqualTo(PartnerMemberStatus.ACTIVE);
            assertThat(member.isActive()).isTrue();
            assertThat(member.role()).isEqualTo(PartnerMemberRole.OWNER);
            assertThat(member.partnerId()).isEqualTo(PartnerId.of(1L));
        }

        @Test
        @DisplayName("partnerId가 null이면 예외가 발생한다")
        void shouldThrowWhenPartnerIdIsNull() {
            assertThatThrownBy(() -> PartnerMember.forNew(
                    null, PartnerFixture.DEFAULT_MEMBER_NAME, PartnerFixture.DEFAULT_EMAIL,
                    PartnerFixture.DEFAULT_PHONE, PartnerMemberRole.STAFF, NOW
            )).isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("파트너 ID는 필수");
        }

        @Test
        @DisplayName("role이 null이면 예외가 발생한다")
        void shouldThrowWhenRoleIsNull() {
            assertThatThrownBy(() -> PartnerMember.forNew(
                    PartnerId.of(1L), PartnerFixture.DEFAULT_MEMBER_NAME, PartnerFixture.DEFAULT_EMAIL,
                    PartnerFixture.DEFAULT_PHONE, null, NOW
            )).isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("역할은 필수");
        }

        @Test
        @DisplayName("신규 멤버의 ID는 null 값을 가진 PartnerMemberId이다")
        void shouldHaveNullId() {
            PartnerMember member = PartnerFixture.activeOwnerMember();

            assertThat(member.id()).isNotNull();
            assertThat(member.id().value()).isNull();
        }
    }

    @Nested
    @DisplayName("T-2: reconstitute() 팩토리 메서드")
    class ReconstituteTest {

        @Test
        @DisplayName("DB 복원 시 모든 필드가 그대로 복원된다")
        void shouldReconstituteAllFields() {
            PartnerMemberId id = PartnerMemberId.of(10L);
            PartnerId partnerId = PartnerId.of(1L);
            MemberName name = MemberName.of("김철수");
            Email email = Email.of("kim@test.com");
            PhoneNumber phone = PhoneNumber.of("010-9999-8888");

            PartnerMember member = PartnerMember.reconstitute(
                    id, partnerId, name, email, phone,
                    PartnerMemberRole.MANAGER, PartnerMemberStatus.SUSPENDED,
                    NOW, LATER
            );

            assertThat(member.id()).isEqualTo(id);
            assertThat(member.partnerId()).isEqualTo(partnerId);
            assertThat(member.name()).isEqualTo(name);
            assertThat(member.email()).isEqualTo(email);
            assertThat(member.phone()).isEqualTo(phone);
            assertThat(member.role()).isEqualTo(PartnerMemberRole.MANAGER);
            assertThat(member.status()).isEqualTo(PartnerMemberStatus.SUSPENDED);
            assertThat(member.createdAt()).isEqualTo(NOW);
            assertThat(member.updatedAt()).isEqualTo(LATER);
        }
    }

    @Nested
    @DisplayName("T-3: suspend() 상태 전이")
    class SuspendTest {

        @Test
        @DisplayName("ACTIVE 멤버를 정지하면 SUSPENDED 상태로 변경된다")
        void shouldSuspendActiveMember() {
            PartnerMember member = PartnerFixture.reconstitutedMember();

            member.suspend(LATER);

            assertThat(member.status()).isEqualTo(PartnerMemberStatus.SUSPENDED);
            assertThat(member.isActive()).isFalse();
            assertThat(member.updatedAt()).isEqualTo(LATER);
        }

        @Test
        @DisplayName("이미 SUSPENDED인 멤버를 정지하면 예외가 발생한다")
        void shouldThrowWhenAlreadySuspended() {
            PartnerMember member = PartnerFixture.suspendedMember();

            assertThatThrownBy(() -> member.suspend(LATER))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이미 정지된 멤버");
        }
    }

    @Nested
    @DisplayName("T-4: activate() 상태 전이")
    class ActivateTest {

        @Test
        @DisplayName("SUSPENDED 멤버를 활성화하면 ACTIVE 상태로 변경된다")
        void shouldActivateSuspendedMember() {
            PartnerMember member = PartnerFixture.suspendedMember();

            member.activate(LATER);

            assertThat(member.status()).isEqualTo(PartnerMemberStatus.ACTIVE);
            assertThat(member.isActive()).isTrue();
            assertThat(member.updatedAt()).isEqualTo(LATER);
        }

        @Test
        @DisplayName("이미 ACTIVE인 멤버를 활성화하면 예외가 발생한다")
        void shouldThrowWhenAlreadyActive() {
            PartnerMember member = PartnerFixture.reconstitutedMember();

            assertThatThrownBy(() -> member.activate(LATER))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이미 활성 상태인 멤버");
        }
    }

    @Nested
    @DisplayName("T-5: changeRole() 역할 변경")
    class ChangeRoleTest {

        @Test
        @DisplayName("STAFF에서 MANAGER로 역할을 변경할 수 있다")
        void shouldChangeRole() {
            PartnerMember member = PartnerFixture.activeStaffMember();

            member.changeRole(PartnerMemberRole.MANAGER, LATER);

            assertThat(member.role()).isEqualTo(PartnerMemberRole.MANAGER);
            assertThat(member.updatedAt()).isEqualTo(LATER);
        }

        @Test
        @DisplayName("null 역할로 변경하면 예외가 발생한다")
        void shouldThrowWhenRoleIsNull() {
            PartnerMember member = PartnerFixture.activeStaffMember();

            assertThatThrownBy(() -> member.changeRole(null, LATER))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("역할은 필수");
        }
    }

    @Nested
    @DisplayName("T-6: updateProfile() 프로필 수정")
    class UpdateProfileTest {

        @Test
        @DisplayName("프로필 정보를 수정할 수 있다")
        void shouldUpdateProfile() {
            PartnerMember member = PartnerFixture.reconstitutedMember();
            MemberName newName = MemberName.of("새이름");
            Email newEmail = Email.of("new@test.com");
            PhoneNumber newPhone = PhoneNumber.of("010-0000-0000");

            member.updateProfile(newName, newEmail, newPhone, LATER);

            assertThat(member.name()).isEqualTo(newName);
            assertThat(member.email()).isEqualTo(newEmail);
            assertThat(member.phone()).isEqualTo(newPhone);
            assertThat(member.updatedAt()).isEqualTo(LATER);
        }
    }

    @Nested
    @DisplayName("T-7: equals/hashCode")
    class EqualsHashCodeTest {

        @Test
        @DisplayName("같은 ID를 가진 PartnerMember는 동등하다")
        void sameIdShouldBeEqual() {
            PartnerMember m1 = PartnerFixture.reconstitutedMember();
            PartnerMember m2 = PartnerMember.reconstitute(
                    PartnerMemberId.of(1L), PartnerId.of(99L), MemberName.of("다른사람"),
                    Email.of("other@test.com"), PhoneNumber.of("010-0000-0000"),
                    PartnerMemberRole.OWNER, PartnerMemberStatus.SUSPENDED,
                    NOW, LATER
            );

            assertThat(m1).isEqualTo(m2);
            assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
        }

        @Test
        @DisplayName("다른 ID를 가진 PartnerMember는 동등하지 않다")
        void differentIdShouldNotBeEqual() {
            PartnerMember m1 = PartnerFixture.reconstitutedMember();
            PartnerMember m2 = PartnerMember.reconstitute(
                    PartnerMemberId.of(2L), PartnerId.of(1L), PartnerFixture.DEFAULT_MEMBER_NAME,
                    PartnerFixture.DEFAULT_EMAIL, PartnerFixture.DEFAULT_PHONE,
                    PartnerMemberRole.STAFF, PartnerMemberStatus.ACTIVE,
                    NOW, NOW
            );

            assertThat(m1).isNotEqualTo(m2);
        }

        @Test
        @DisplayName("forNew()로 만든 두 멤버는 id.value가 null이므로 equals false")
        void forNewMembersShouldNotBeEqual() {
            PartnerMember m1 = PartnerFixture.activeOwnerMember();
            PartnerMember m2 = PartnerFixture.activeStaffMember();

            assertThat(m1).isNotEqualTo(m2);
        }
    }
}
