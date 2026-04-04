package com.ryuqq.otatoy.domain.partner;

import com.ryuqq.otatoy.domain.common.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PartnerErrorCodeTest {

    @Nested
    @DisplayName("T-5: ErrorCode Enum 검증")
    class ErrorCodeValidation {

        @Test
        @DisplayName("PARTNER_NOT_FOUND: PTN-001, 404")
        void partnerNotFound() {
            PartnerErrorCode code = PartnerErrorCode.PARTNER_NOT_FOUND;

            assertThat(code.getCode()).isEqualTo("PTN-001");
            assertThat(code.getMessage()).isEqualTo("파트너를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("PARTNER_ALREADY_SUSPENDED: PTN-002, 409")
        void partnerAlreadySuspended() {
            PartnerErrorCode code = PartnerErrorCode.PARTNER_ALREADY_SUSPENDED;

            assertThat(code.getCode()).isEqualTo("PTN-002");
            assertThat(code.getMessage()).isEqualTo("이미 정지된 파트너입니다");
        }

        @Test
        @DisplayName("PARTNER_ALREADY_ACTIVE: PTN-003, 409")
        void partnerAlreadyActive() {
            PartnerErrorCode code = PartnerErrorCode.PARTNER_ALREADY_ACTIVE;

            assertThat(code.getCode()).isEqualTo("PTN-003");
            assertThat(code.getMessage()).isEqualTo("이미 활성 상태인 파트너입니다");
        }

        @Test
        @DisplayName("PARTNER_MEMBER_NOT_FOUND: PTN-004, 404")
        void partnerMemberNotFound() {
            PartnerErrorCode code = PartnerErrorCode.PARTNER_MEMBER_NOT_FOUND;

            assertThat(code.getCode()).isEqualTo("PTN-004");
            assertThat(code.getMessage()).isEqualTo("파트너 멤버를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("모든 ErrorCode는 ErrorCode 인터페이스를 구현한다")
        void shouldImplementErrorCodeInterface() {
            for (PartnerErrorCode code : PartnerErrorCode.values()) {
                assertThat(code).isInstanceOf(ErrorCode.class);
            }
        }

        @Test
        @DisplayName("모든 ErrorCode의 code는 PTN- 접두사로 시작한다")
        void allCodesShouldStartWithPtnPrefix() {
            for (PartnerErrorCode code : PartnerErrorCode.values()) {
                assertThat(code.getCode())
                        .as("ErrorCode %s의 code", code.name())
                        .startsWith("PTN-");
            }
        }

        @Test
        @DisplayName("모든 ErrorCode의 message는 null이 아니고 비어있지 않다")
        void allCodesShouldHaveNonEmptyMessage() {
            for (PartnerErrorCode code : PartnerErrorCode.values()) {
                assertThat(code.getMessage())
                        .as("ErrorCode %s의 message", code.name())
                        .isNotNull()
                        .isNotBlank();
            }
        }
    }
}
