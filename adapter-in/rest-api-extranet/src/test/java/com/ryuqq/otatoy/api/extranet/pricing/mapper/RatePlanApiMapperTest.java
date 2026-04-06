package com.ryuqq.otatoy.api.extranet.pricing.mapper;

import com.ryuqq.otatoy.api.extranet.pricing.dto.request.RegisterRatePlanApiRequest;
import com.ryuqq.otatoy.application.pricing.dto.command.RegisterRatePlanCommand;
import com.ryuqq.otatoy.domain.pricing.PaymentPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RatePlanApiMapper 단위 테스트.
 * 요금 정책 등록 Request -> Command 변환 정확성을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@DisplayName("RatePlanApiMapper")
class RatePlanApiMapperTest {

    @Nested
    @DisplayName("toCommand")
    class ToCommand {

        @Nested
        @DisplayName("전체 필드가 포함된 요청")
        class AllFields {

            @Test
            @DisplayName("모든 필드가 정확하게 변환된다")
            void shouldMapAllFieldsToCommand() {
                // given
                var request = new RegisterRatePlanApiRequest(
                    "스탠다드 요금", true, false, 3,
                    "체크인 3일 전까지 무료 취소", "PREPAY"
                );

                // when
                RegisterRatePlanCommand command = RatePlanApiMapper.toCommand(10L, request);

                // then
                assertThat(command.roomTypeId().value()).isEqualTo(10L);
                assertThat(command.name().value()).isEqualTo("스탠다드 요금");
                assertThat(command.paymentPolicy()).isEqualTo(PaymentPolicy.PREPAY);
            }
        }

        @Nested
        @DisplayName("CancellationPolicy 변환")
        class CancellationPolicyMapping {

            @Test
            @DisplayName("freeCancellation이 true이면 CancellationPolicy에 반영된다")
            void shouldMapFreeCancellationTrue() {
                var request = new RegisterRatePlanApiRequest(
                    "무료 취소 요금", true, false, 3,
                    "3일 전 무료 취소", "PREPAY"
                );

                RegisterRatePlanCommand command = RatePlanApiMapper.toCommand(1L, request);

                assertThat(command.cancellationPolicy().freeCancellation()).isTrue();
                assertThat(command.cancellationPolicy().nonRefundable()).isFalse();
                assertThat(command.cancellationPolicy().deadlineDays()).isEqualTo(3);
                assertThat(command.cancellationPolicy().policyText()).isEqualTo("3일 전 무료 취소");
            }

            @Test
            @DisplayName("nonRefundable이 true이면 CancellationPolicy에 반영된다")
            void shouldMapNonRefundableTrue() {
                var request = new RegisterRatePlanApiRequest(
                    "환불 불가 요금", false, true, 0,
                    null, "PREPAY"
                );

                RegisterRatePlanCommand command = RatePlanApiMapper.toCommand(1L, request);

                assertThat(command.cancellationPolicy().freeCancellation()).isFalse();
                assertThat(command.cancellationPolicy().nonRefundable()).isTrue();
            }
        }

        @Nested
        @DisplayName("nullable 필드 기본값 처리")
        class NullableDefaults {

            @Test
            @DisplayName("freeCancellation이 null이면 false로 처리된다")
            void shouldDefaultFreeCancellationToFalse() {
                var request = new RegisterRatePlanApiRequest(
                    "요금", null, null, null,
                    null, "PAY_AT_PROPERTY"
                );

                RegisterRatePlanCommand command = RatePlanApiMapper.toCommand(1L, request);

                assertThat(command.cancellationPolicy().freeCancellation()).isFalse();
            }

            @Test
            @DisplayName("nonRefundable이 null이면 false로 처리된다")
            void shouldDefaultNonRefundableToFalse() {
                var request = new RegisterRatePlanApiRequest(
                    "요금", null, null, null,
                    null, "PAY_AT_PROPERTY"
                );

                RegisterRatePlanCommand command = RatePlanApiMapper.toCommand(1L, request);

                assertThat(command.cancellationPolicy().nonRefundable()).isFalse();
            }

            @Test
            @DisplayName("freeCancellationDeadlineDays가 null이면 0으로 처리된다")
            void shouldDefaultDeadlineDaysToZero() {
                var request = new RegisterRatePlanApiRequest(
                    "요금", null, null, null,
                    null, "PAY_AT_PROPERTY"
                );

                RegisterRatePlanCommand command = RatePlanApiMapper.toCommand(1L, request);

                assertThat(command.cancellationPolicy().deadlineDays()).isZero();
            }
        }

        @Nested
        @DisplayName("PaymentPolicy Enum 변환")
        class PaymentPolicyMapping {

            @Test
            @DisplayName("PREPAY 문자열이 PaymentPolicy.PREPAY로 변환된다")
            void shouldMapPrepay() {
                var request = new RegisterRatePlanApiRequest(
                    "요금", null, null, null, null, "PREPAY"
                );

                RegisterRatePlanCommand command = RatePlanApiMapper.toCommand(1L, request);

                assertThat(command.paymentPolicy()).isEqualTo(PaymentPolicy.PREPAY);
            }

            @Test
            @DisplayName("PAY_AT_PROPERTY 문자열이 PaymentPolicy.PAY_AT_PROPERTY로 변환된다")
            void shouldMapPayAtProperty() {
                var request = new RegisterRatePlanApiRequest(
                    "요금", null, null, null, null, "PAY_AT_PROPERTY"
                );

                RegisterRatePlanCommand command = RatePlanApiMapper.toCommand(1L, request);

                assertThat(command.paymentPolicy()).isEqualTo(PaymentPolicy.PAY_AT_PROPERTY);
            }

            @Test
            @DisplayName("PAY_BEFORE_CHECKIN 문자열이 PaymentPolicy.PAY_BEFORE_CHECKIN으로 변환된다")
            void shouldMapPayBeforeCheckin() {
                var request = new RegisterRatePlanApiRequest(
                    "요금", null, null, null, null, "PAY_BEFORE_CHECKIN"
                );

                RegisterRatePlanCommand command = RatePlanApiMapper.toCommand(1L, request);

                assertThat(command.paymentPolicy()).isEqualTo(PaymentPolicy.PAY_BEFORE_CHECKIN);
            }
        }
    }
}
