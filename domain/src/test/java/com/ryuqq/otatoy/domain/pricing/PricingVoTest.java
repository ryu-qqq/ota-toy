package com.ryuqq.otatoy.domain.pricing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PricingVoTest {

    @Nested
    @DisplayName("T-5: RatePlanName VO")
    class RatePlanNameTest {

        @Test
        @DisplayName("정상 값으로 생성 성공")
        void shouldCreateWithValidValue() {
            RatePlanName name = RatePlanName.of("기본 요금제");
            assertThat(name.value()).isEqualTo("기본 요금제");
        }

        @Test
        @DisplayName("null이면 예외를 던진다")
        void shouldFailWhenNull() {
            assertThatThrownBy(() -> RatePlanName.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("요금 정책명은 필수");
        }

        @Test
        @DisplayName("빈 문자열이면 예외를 던진다")
        void shouldFailWhenBlank() {
            assertThatThrownBy(() -> RatePlanName.of("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("요금 정책명은 필수");
        }
    }

    @Nested
    @DisplayName("T-5: AddOnName VO")
    class AddOnNameTest {

        @Test
        @DisplayName("정상 값으로 생성 성공")
        void shouldCreateWithValidValue() {
            AddOnName name = AddOnName.of("조식 뷔페");
            assertThat(name.value()).isEqualTo("조식 뷔페");
        }

        @Test
        @DisplayName("null이면 예외를 던진다")
        void shouldFailWhenNull() {
            assertThatThrownBy(() -> AddOnName.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Add-on 이름은 필수");
        }

        @Test
        @DisplayName("빈 문자열이면 예외를 던진다")
        void shouldFailWhenBlank() {
            assertThatThrownBy(() -> AddOnName.of(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Add-on 이름은 필수");
        }
    }

    @Nested
    @DisplayName("T-5: AddOnType VO")
    class AddOnTypeTest {

        @Test
        @DisplayName("정상 값으로 생성 성공")
        void shouldCreateWithValidValue() {
            AddOnType type = AddOnType.of("BREAKFAST");
            assertThat(type.value()).isEqualTo("BREAKFAST");
        }

        @Test
        @DisplayName("null이면 예외를 던진다")
        void shouldFailWhenNull() {
            assertThatThrownBy(() -> AddOnType.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Add-on 유형은 필수");
        }

        @Test
        @DisplayName("빈 문자열이면 예외를 던진다")
        void shouldFailWhenBlank() {
            assertThatThrownBy(() -> AddOnType.of("  "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Add-on 유형은 필수");
        }
    }

    @Nested
    @DisplayName("T-5: RatePlanId VO")
    class RatePlanIdTest {

        @Test
        @DisplayName("null 값으로 생성 시 isNew() = true")
        void shouldBeNewWhenNull() {
            RatePlanId id = RatePlanId.of(null);
            assertThat(id.isNew()).isTrue();
            assertThat(id.value()).isNull();
        }

        @Test
        @DisplayName("값이 있으면 isNew() = false")
        void shouldNotBeNewWhenValueExists() {
            RatePlanId id = RatePlanId.of(1L);
            assertThat(id.isNew()).isFalse();
            assertThat(id.value()).isEqualTo(1L);
        }

        @Test
        @DisplayName("같은 값의 ID는 동등하다 (Record 특성)")
        void shouldBeEqualWithSameValue() {
            RatePlanId a = RatePlanId.of(1L);
            RatePlanId b = RatePlanId.of(1L);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }
    }

    @Nested
    @DisplayName("T-5: PricingErrorCode")
    class PricingErrorCodeTest {

        @Test
        @DisplayName("ErrorCode 인터페이스를 구현한다")
        void shouldImplementErrorCode() {
            PricingErrorCode code = PricingErrorCode.RATE_PLAN_NOT_FOUND;

            assertThat(code.getCode()).isEqualTo("PRC-001");
            assertThat(code.getMessage()).isEqualTo("요금 정책을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("모든 ErrorCode가 PRC- 접두사를 가진다")
        void shouldAllHavePrcPrefix() {
            for (PricingErrorCode code : PricingErrorCode.values()) {
                assertThat(code.getCode()).startsWith("PRC-");
            }
        }
    }

    @Nested
    @DisplayName("T-5: Enum displayName()")
    class EnumDisplayNameTest {

        @Test
        @DisplayName("PaymentPolicy에 한국어 displayName이 있다")
        void paymentPolicyShouldHaveDisplayName() {
            assertThat(PaymentPolicy.PREPAY.displayName()).isEqualTo("선결제");
            assertThat(PaymentPolicy.PAY_AT_PROPERTY.displayName()).isEqualTo("현장결제");
            assertThat(PaymentPolicy.PAY_BEFORE_CHECKIN.displayName()).isEqualTo("체크인 전 결제");
        }

        @Test
        @DisplayName("SourceType에 한국어 displayName이 있다")
        void sourceTypeShouldHaveDisplayName() {
            assertThat(SourceType.DIRECT.displayName()).isEqualTo("직접 입점");
            assertThat(SourceType.SUPPLIER.displayName()).isEqualTo("외부 공급자");
        }
    }
}
