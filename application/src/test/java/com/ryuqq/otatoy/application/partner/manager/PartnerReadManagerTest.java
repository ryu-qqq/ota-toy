package com.ryuqq.otatoy.application.partner.manager;

import com.ryuqq.otatoy.application.partner.port.out.PartnerQueryPort;
import com.ryuqq.otatoy.domain.partner.Partner;
import com.ryuqq.otatoy.domain.partner.PartnerFixture;
import com.ryuqq.otatoy.domain.partner.PartnerId;
import com.ryuqq.otatoy.domain.partner.PartnerNotFoundException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

/**
 * PartnerReadManager 단위 테스트.
 * PartnerQueryPort를 Mock으로 대체하여 조회/검증 로직을 확인한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class PartnerReadManagerTest {

    @Mock
    PartnerQueryPort partnerQueryPort;

    @InjectMocks
    PartnerReadManager manager;

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("존재하는 ID로 조회하면 Partner를 반환한다")
        void shouldReturnPartnerWhenFound() {
            // given
            PartnerId id = PartnerId.of(1L);
            Partner expected = PartnerFixture.reconstitutedPartner();
            given(partnerQueryPort.findById(id)).willReturn(Optional.of(expected));

            // when
            Partner result = manager.getById(id);

            // then
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 PartnerNotFoundException을 던진다")
        void shouldThrowWhenNotFound() {
            // given
            PartnerId id = PartnerId.of(999L);
            given(partnerQueryPort.findById(id)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> manager.getById(id))
                .isInstanceOf(PartnerNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("verifyExists")
    class VerifyExists {

        @Test
        @DisplayName("존재하는 ID이면 예외 없이 통과한다")
        void shouldPassWhenExists() {
            // given
            PartnerId id = PartnerId.of(1L);
            given(partnerQueryPort.existsById(id)).willReturn(true);

            // when & then
            assertThatCode(() -> manager.verifyExists(id))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("존재하지 않는 ID이면 PartnerNotFoundException을 던진다")
        void shouldThrowWhenNotExists() {
            // given
            PartnerId id = PartnerId.of(999L);
            given(partnerQueryPort.existsById(id)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> manager.verifyExists(id))
                .isInstanceOf(PartnerNotFoundException.class);
        }
    }
}
