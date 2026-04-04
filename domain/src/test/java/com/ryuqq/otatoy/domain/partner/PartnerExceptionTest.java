package com.ryuqq.otatoy.domain.partner;

import com.ryuqq.otatoy.domain.common.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PartnerExceptionTest {

    @Nested
    @DisplayName("T-9: 도메인 Exception 검증")
    class ExceptionValidation {

        @Test
        @DisplayName("PartnerNotFoundException은 DomainException을 상속한다")
        void partnerNotFoundShouldExtendDomainException() {
            PartnerNotFoundException ex = new PartnerNotFoundException();

            assertThat(ex).isInstanceOf(DomainException.class);
            assertThat(ex.getErrorCode()).isEqualTo(PartnerErrorCode.PARTNER_NOT_FOUND);
            assertThat(ex.getMessage()).isEqualTo("파트너를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("PartnerAlreadySuspendedException은 DomainException을 상속한다")
        void alreadySuspendedShouldExtendDomainException() {
            PartnerAlreadySuspendedException ex = new PartnerAlreadySuspendedException();

            assertThat(ex).isInstanceOf(DomainException.class);
            assertThat(ex.getErrorCode()).isEqualTo(PartnerErrorCode.PARTNER_ALREADY_SUSPENDED);
            assertThat(ex.getMessage()).isEqualTo("이미 정지된 파트너입니다");
        }

        @Test
        @DisplayName("PartnerAlreadyActiveException은 DomainException을 상속한다")
        void alreadyActiveShouldExtendDomainException() {
            PartnerAlreadyActiveException ex = new PartnerAlreadyActiveException();

            assertThat(ex).isInstanceOf(DomainException.class);
            assertThat(ex.getErrorCode()).isEqualTo(PartnerErrorCode.PARTNER_ALREADY_ACTIVE);
            assertThat(ex.getMessage()).isEqualTo("이미 활성 상태인 파트너입니다");
        }

        @Test
        @DisplayName("PartnerMemberNotFoundException은 DomainException을 상속한다")
        void memberNotFoundShouldExtendDomainException() {
            PartnerMemberNotFoundException ex = new PartnerMemberNotFoundException();

            assertThat(ex).isInstanceOf(DomainException.class);
            assertThat(ex.getErrorCode()).isEqualTo(PartnerErrorCode.PARTNER_MEMBER_NOT_FOUND);
            assertThat(ex.getMessage()).isEqualTo("파트너 멤버를 찾을 수 없습니다");
        }
    }
}
