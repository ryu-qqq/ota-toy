package com.ryuqq.otatoy.domain.partner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PartnerTest {

    private static final Instant NOW = PartnerFixture.DEFAULT_NOW;
    private static final Instant LATER = NOW.plusSeconds(3600);

    @Nested
    @DisplayName("T-1: forNew() 팩토리 메서드")
    class ForNewTest {

        @Test
        @DisplayName("신규 파트너는 ACTIVE 상태로 생성된다")
        void shouldCreateActivePartner() {
            Partner partner = Partner.forNew(PartnerName.of("테스트"), NOW);

            assertThat(partner.status()).isEqualTo(PartnerStatus.ACTIVE);
            assertThat(partner.isActive()).isTrue();
            assertThat(partner.name().value()).isEqualTo("테스트");
            assertThat(partner.createdAt()).isEqualTo(NOW);
            assertThat(partner.updatedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("신규 파트너의 ID는 null 값을 가진 PartnerId이다")
        void shouldHaveNullId() {
            Partner partner = Partner.forNew(PartnerName.of("테스트"), NOW);

            assertThat(partner.id()).isNotNull();
            assertThat(partner.id().value()).isNull();
        }
    }

    @Nested
    @DisplayName("T-2: reconstitute() 팩토리 메서드")
    class ReconstituteTest {

        @Test
        @DisplayName("DB 복원 시 모든 필드가 그대로 복원된다")
        void shouldReconstituteAllFields() {
            PartnerId id = PartnerId.of(1L);
            PartnerName name = PartnerName.of("복원파트너");
            PartnerStatus status = PartnerStatus.SUSPENDED;

            Partner partner = Partner.reconstitute(id, name, status, NOW, LATER);

            assertThat(partner.id()).isEqualTo(id);
            assertThat(partner.name()).isEqualTo(name);
            assertThat(partner.status()).isEqualTo(status);
            assertThat(partner.createdAt()).isEqualTo(NOW);
            assertThat(partner.updatedAt()).isEqualTo(LATER);
        }
    }

    @Nested
    @DisplayName("T-3: suspend() 상태 전이")
    class SuspendTest {

        @Test
        @DisplayName("ACTIVE 파트너를 정지하면 SUSPENDED 상태로 변경된다")
        void shouldSuspendActivePartner() {
            Partner partner = PartnerFixture.reconstitutedPartner();

            partner.suspend(LATER);

            assertThat(partner.status()).isEqualTo(PartnerStatus.SUSPENDED);
            assertThat(partner.isActive()).isFalse();
            assertThat(partner.updatedAt()).isEqualTo(LATER);
        }

        @Test
        @DisplayName("이미 SUSPENDED인 파트너를 정지하면 PartnerAlreadySuspendedException이 발생한다")
        void shouldThrowWhenAlreadySuspended() {
            Partner partner = PartnerFixture.suspendedPartner();

            assertThatThrownBy(() -> partner.suspend(LATER))
                    .isInstanceOf(PartnerAlreadySuspendedException.class);
        }
    }

    @Nested
    @DisplayName("T-4: activate() 상태 전이")
    class ActivateTest {

        @Test
        @DisplayName("SUSPENDED 파트너를 활성화하면 ACTIVE 상태로 변경된다")
        void shouldActivateSuspendedPartner() {
            Partner partner = PartnerFixture.suspendedPartner();

            partner.activate(LATER);

            assertThat(partner.status()).isEqualTo(PartnerStatus.ACTIVE);
            assertThat(partner.isActive()).isTrue();
            assertThat(partner.updatedAt()).isEqualTo(LATER);
        }

        @Test
        @DisplayName("이미 ACTIVE인 파트너를 활성화하면 PartnerAlreadyActiveException이 발생한다")
        void shouldThrowWhenAlreadyActive() {
            Partner partner = PartnerFixture.reconstitutedPartner();

            assertThatThrownBy(() -> partner.activate(LATER))
                    .isInstanceOf(PartnerAlreadyActiveException.class);
        }
    }

    @Nested
    @DisplayName("T-5: equals/hashCode")
    class EqualsHashCodeTest {

        @Test
        @DisplayName("같은 ID를 가진 Partner는 동등하다")
        void sameIdShouldBeEqual() {
            Partner p1 = PartnerFixture.partnerWithId(1L);
            Partner p2 = Partner.reconstitute(
                    PartnerId.of(1L), PartnerName.of("다른이름"), PartnerStatus.SUSPENDED, NOW, LATER
            );

            assertThat(p1).isEqualTo(p2);
            assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
        }

        @Test
        @DisplayName("다른 ID를 가진 Partner는 동등하지 않다")
        void differentIdShouldNotBeEqual() {
            Partner p1 = PartnerFixture.partnerWithId(1L);
            Partner p2 = PartnerFixture.partnerWithId(2L);

            assertThat(p1).isNotEqualTo(p2);
        }

        @Test
        @DisplayName("forNew()로 만든 두 객체는 id가 null이므로 equals false")
        void forNewPartnersShouldNotBeEqual() {
            Partner p1 = Partner.forNew(PartnerName.of("A"), NOW);
            Partner p2 = Partner.forNew(PartnerName.of("B"), NOW);

            assertThat(p1).isNotEqualTo(p2);
        }
    }
}
