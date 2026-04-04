package com.ryuqq.otatoy.domain.partner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PartnerEnumTest {

    @Nested
    @DisplayName("T-6: PartnerStatus Enum")
    class PartnerStatusTest {

        @Test
        @DisplayName("ACTIVE의 displayName은 '활성'이다")
        void activeDisplayName() {
            assertThat(PartnerStatus.ACTIVE.displayName()).isEqualTo("활성");
        }

        @Test
        @DisplayName("SUSPENDED의 displayName은 '정지'이다")
        void suspendedDisplayName() {
            assertThat(PartnerStatus.SUSPENDED.displayName()).isEqualTo("정지");
        }

        @Test
        @DisplayName("모든 PartnerStatus는 비어있지 않은 displayName을 가진다")
        void allShouldHaveDisplayName() {
            for (PartnerStatus status : PartnerStatus.values()) {
                assertThat(status.displayName()).isNotBlank();
            }
        }
    }

    @Nested
    @DisplayName("T-7: PartnerMemberRole Enum")
    class PartnerMemberRoleTest {

        @Test
        @DisplayName("OWNER의 displayName은 '소유자'이다")
        void ownerDisplayName() {
            assertThat(PartnerMemberRole.OWNER.displayName()).isEqualTo("소유자");
        }

        @Test
        @DisplayName("MANAGER의 displayName은 '관리자'이다")
        void managerDisplayName() {
            assertThat(PartnerMemberRole.MANAGER.displayName()).isEqualTo("관리자");
        }

        @Test
        @DisplayName("STAFF의 displayName은 '직원'이다")
        void staffDisplayName() {
            assertThat(PartnerMemberRole.STAFF.displayName()).isEqualTo("직원");
        }

        @Test
        @DisplayName("모든 PartnerMemberRole은 비어있지 않은 displayName을 가진다")
        void allShouldHaveDisplayName() {
            for (PartnerMemberRole role : PartnerMemberRole.values()) {
                assertThat(role.displayName()).isNotBlank();
            }
        }
    }

    @Nested
    @DisplayName("T-8: PartnerMemberStatus Enum")
    class PartnerMemberStatusTest {

        @Test
        @DisplayName("ACTIVE의 displayName은 '활성'이다")
        void activeDisplayName() {
            assertThat(PartnerMemberStatus.ACTIVE.displayName()).isEqualTo("활성");
        }

        @Test
        @DisplayName("SUSPENDED의 displayName은 '정지'이다")
        void suspendedDisplayName() {
            assertThat(PartnerMemberStatus.SUSPENDED.displayName()).isEqualTo("정지");
        }
    }
}
