package com.ryuqq.otatoy.domain.pricing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.ryuqq.otatoy.domain.pricing.PricingFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RatePlanAddOnTest {

    @Nested
    @DisplayName("T-1: forNew() 생성 검증")
    class Creation {

        @Test
        @DisplayName("유료 Add-on 정상 생성")
        void shouldCreatePaidAddOnSuccessfully() {
            RatePlanAddOn addOn = breakfastAddOn();

            assertThat(addOn.id().isNew()).isTrue();
            assertThat(addOn.ratePlanId()).isEqualTo(RATE_PLAN_ID);
            assertThat(addOn.addOnType()).isEqualTo(AddOnType.of("BREAKFAST"));
            assertThat(addOn.name()).isEqualTo(AddOnName.of("조식 뷔페"));
            assertThat(addOn.price()).isEqualByComparingTo(BigDecimal.valueOf(23_000));
            assertThat(addOn.included()).isFalse();
            assertThat(addOn.createdAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("무료 포함 Add-on 정상 생성")
        void shouldCreateFreeIncludedAddOnSuccessfully() {
            RatePlanAddOn addOn = freeIncludedAddOn();

            assertThat(addOn.price()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(addOn.included()).isTrue();
        }

        @Test
        @DisplayName("price가 음수이면 예외를 던진다")
        void shouldFailWhenPriceIsNegative() {
            assertThatThrownBy(() -> RatePlanAddOn.forNew(
                    RATE_PLAN_ID, AddOnType.of("BREAKFAST"),
                    AddOnName.of("조식"), BigDecimal.valueOf(-1), false, NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Add-on 가격은 0 이상");
        }

        @Test
        @DisplayName("price가 null이고 included=true이면 허용된다")
        void shouldAllowNullPriceWhenIncluded() {
            RatePlanAddOn addOn = RatePlanAddOn.forNew(
                    RATE_PLAN_ID, AddOnType.of("PARKING"),
                    AddOnName.of("주차"), null, true, NOW
            );

            assertThat(addOn.price()).isNull();
        }

        @Test
        @DisplayName("included=true이고 price > 0이면 예외를 던진다")
        void shouldFailWhenIncludedWithPositivePrice() {
            assertThatThrownBy(() -> RatePlanAddOn.forNew(
                    RATE_PLAN_ID, AddOnType.of("BREAKFAST"),
                    AddOnName.of("조식"), BigDecimal.valueOf(10_000), true, NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("포함된 Add-on은 별도 가격을 가질 수 없습니다");
        }

        @Test
        @DisplayName("included=false이고 price가 null이면 예외를 던진다")
        void shouldFailWhenNotIncludedWithNullPrice() {
            assertThatThrownBy(() -> RatePlanAddOn.forNew(
                    RATE_PLAN_ID, AddOnType.of("BREAKFAST"),
                    AddOnName.of("조식"), null, false, NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("별도 구매 Add-on은 가격이 필수입니다");
        }

        @Test
        @DisplayName("included=false이고 price가 0이면 예외를 던진다")
        void shouldFailWhenNotIncludedWithZeroPrice() {
            assertThatThrownBy(() -> RatePlanAddOn.forNew(
                    RATE_PLAN_ID, AddOnType.of("BREAKFAST"),
                    AddOnName.of("조식"), BigDecimal.ZERO, false, NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("별도 구매 Add-on은 가격이 필수입니다");
        }
    }

    @Nested
    @DisplayName("T-4: isFree() 비즈니스 로직")
    class IsFree {

        @Test
        @DisplayName("price가 0이면 무료이다")
        void shouldBeFreeWhenPriceIsZero() {
            RatePlanAddOn addOn = freeIncludedAddOn();
            assertThat(addOn.isFree()).isTrue();
        }

        @Test
        @DisplayName("price가 null이면 무료이다")
        void shouldBeFreeWhenPriceIsNull() {
            RatePlanAddOn addOn = RatePlanAddOn.forNew(
                    RATE_PLAN_ID, AddOnType.of("WIFI"),
                    AddOnName.of("와이파이"), null, true, NOW
            );
            assertThat(addOn.isFree()).isTrue();
        }

        @Test
        @DisplayName("price가 양수이면 유료이다")
        void shouldNotBeFreeWhenPriceIsPositive() {
            RatePlanAddOn addOn = breakfastAddOn();
            assertThat(addOn.isFree()).isFalse();
        }
    }

    @Nested
    @DisplayName("T-3: reconstitute() 복원")
    class Reconstitute {

        @Test
        @DisplayName("검증 없이 모든 필드를 복원한다")
        void shouldReconstituteFaithfully() {
            RatePlanAddOn addOn = reconstitutedAddOn(42L);

            assertThat(addOn.id()).isEqualTo(RatePlanAddOnId.of(42L));
            assertThat(addOn.ratePlanId()).isEqualTo(RATE_PLAN_ID);
            assertThat(addOn.addOnType()).isEqualTo(AddOnType.of("BREAKFAST"));
            assertThat(addOn.name()).isEqualTo(AddOnName.of("조식 뷔페"));
            assertThat(addOn.price()).isEqualByComparingTo(BigDecimal.valueOf(23_000));
            assertThat(addOn.included()).isFalse();
        }
    }

    @Nested
    @DisplayName("T-6: equals/hashCode")
    class EqualsHashCode {

        @Test
        @DisplayName("같은 ID의 RatePlanAddOn은 동등하다")
        void shouldBeEqualWithSameId() {
            RatePlanAddOn a = reconstitutedAddOn(1L);
            RatePlanAddOn b = reconstitutedAddOn(1L);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("다른 ID의 RatePlanAddOn은 동등하지 않다")
        void shouldNotBeEqualWithDifferentId() {
            RatePlanAddOn a = reconstitutedAddOn(1L);
            RatePlanAddOn b = reconstitutedAddOn(2L);

            assertThat(a).isNotEqualTo(b);
        }
    }
}
