package com.ryuqq.otatoy.application.property.validator;

import com.ryuqq.otatoy.application.property.dto.command.RegisterPropertyCommand;
import com.ryuqq.otatoy.application.property.dto.command.RegisterPropertyCommandFixture;
import com.ryuqq.otatoy.application.partner.manager.PartnerReadManager;
import com.ryuqq.otatoy.application.propertytype.manager.PropertyTypeReadManager;
import com.ryuqq.otatoy.domain.partner.PartnerNotFoundException;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeNotFoundException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

/**
 * PropertyRegistrationValidator 단위 테스트.
 * ReadManager를 Mock으로 대체하여 검증 로직을 확인한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@ExtendWith(MockitoExtension.class)
class PropertyRegistrationValidatorTest {

    @Mock
    PartnerReadManager partnerReadManager;

    @Mock
    PropertyTypeReadManager propertyTypeReadManager;

    @InjectMocks
    PropertyRegistrationValidator validator;

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("파트너와 숙소유형이 모두 존재하면 예외 없이 통과한다")
        void shouldPassWhenBothExist() {
            // given
            RegisterPropertyCommand command = RegisterPropertyCommandFixture.aRegisterPropertyCommand();
            willDoNothing().given(partnerReadManager).verifyExists(command.partnerId());
            willDoNothing().given(propertyTypeReadManager).verifyExists(command.propertyTypeId());

            // when & then
            assertThatCode(() -> validator.validate(command))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("검증 실패 흐름")
    class Failure {

        @Test
        @DisplayName("파트너가 존재하지 않으면 PartnerNotFoundException을 던진다")
        void shouldThrowWhenPartnerNotFound() {
            // given
            RegisterPropertyCommand command = RegisterPropertyCommandFixture.aRegisterPropertyCommand();
            willThrow(new PartnerNotFoundException())
                .given(partnerReadManager).verifyExists(command.partnerId());

            // when & then
            assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(PartnerNotFoundException.class);
        }

        @Test
        @DisplayName("숙소유형이 존재하지 않으면 PropertyTypeNotFoundException을 던진다")
        void shouldThrowWhenPropertyTypeNotFound() {
            // given
            RegisterPropertyCommand command = RegisterPropertyCommandFixture.aRegisterPropertyCommand();
            willDoNothing().given(partnerReadManager).verifyExists(command.partnerId());
            willThrow(new PropertyTypeNotFoundException())
                .given(propertyTypeReadManager).verifyExists(command.propertyTypeId());

            // when & then
            assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(PropertyTypeNotFoundException.class);
        }

        @Test
        @DisplayName("파트너 검증 실패 시 숙소유형 검증은 호출되지 않는다")
        void shouldNotCheckPropertyTypeWhenPartnerFails() {
            // given
            RegisterPropertyCommand command = RegisterPropertyCommandFixture.aRegisterPropertyCommand();
            willThrow(new PartnerNotFoundException())
                .given(partnerReadManager).verifyExists(command.partnerId());

            // when
            try {
                validator.validate(command);
            } catch (PartnerNotFoundException ignored) {
                // 예외는 무시 -- 호출 검증이 목적
            }

            // then
            then(propertyTypeReadManager).should(never()).verifyExists(command.propertyTypeId());
        }
    }
}
