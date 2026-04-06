package com.ryuqq.otatoy.api.extranet.pricing.mapper;

import com.ryuqq.otatoy.api.extranet.pricing.dto.request.SetRateAndInventoryApiRequest;
import com.ryuqq.otatoy.application.pricing.dto.command.SetRateAndInventoryCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RateAndInventoryApiMapper 단위 테스트.
 * 요금/재고 설정 Request -> Command 변환 정확성을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@DisplayName("RateAndInventoryApiMapper")
class RateAndInventoryApiMapperTest {

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
                var request = new SetRateAndInventoryApiRequest(
                    LocalDate.of(2026, 5, 1),
                    LocalDate.of(2026, 5, 31),
                    new BigDecimal("100000"),
                    new BigDecimal("90000"),
                    new BigDecimal("120000"),
                    new BigDecimal("130000"),
                    new BigDecimal("95000"),
                    10,
                    List.of(
                        new SetRateAndInventoryApiRequest.OverrideItem(
                            LocalDate.of(2026, 5, 5),
                            new BigDecimal("150000"),
                            "어린이날 특가"
                        )
                    )
                );

                // when
                SetRateAndInventoryCommand command = RateAndInventoryApiMapper.toCommand(10L, request);

                // then
                assertThat(command.ratePlanId().value()).isEqualTo(10L);
                assertThat(command.startDate()).isEqualTo(LocalDate.of(2026, 5, 1));
                assertThat(command.endDate()).isEqualTo(LocalDate.of(2026, 5, 31));
                assertThat(command.basePrice()).isEqualByComparingTo(new BigDecimal("100000"));
                assertThat(command.weekdayPrice()).isEqualByComparingTo(new BigDecimal("90000"));
                assertThat(command.fridayPrice()).isEqualByComparingTo(new BigDecimal("120000"));
                assertThat(command.saturdayPrice()).isEqualByComparingTo(new BigDecimal("130000"));
                assertThat(command.sundayPrice()).isEqualByComparingTo(new BigDecimal("95000"));
                assertThat(command.baseInventory()).isEqualTo(10);
            }
        }

        @Nested
        @DisplayName("nullable 요일별 요금")
        class NullableWeekdayPrices {

            @Test
            @DisplayName("weekdayPrice 등이 null이면 Command에서도 null이다")
            void shouldPassNullWeekdayPrices() {
                var request = new SetRateAndInventoryApiRequest(
                    LocalDate.of(2026, 5, 1),
                    LocalDate.of(2026, 5, 31),
                    new BigDecimal("100000"),
                    null, null, null, null,
                    10, null
                );

                SetRateAndInventoryCommand command = RateAndInventoryApiMapper.toCommand(1L, request);

                assertThat(command.weekdayPrice()).isNull();
                assertThat(command.fridayPrice()).isNull();
                assertThat(command.saturdayPrice()).isNull();
                assertThat(command.sundayPrice()).isNull();
            }
        }

        @Nested
        @DisplayName("overrides 리스트 변환")
        class OverridesMapping {

            @Test
            @DisplayName("overrides가 null이면 빈 리스트로 변환된다")
            void shouldMapNullOverridesToEmptyList() {
                var request = new SetRateAndInventoryApiRequest(
                    LocalDate.of(2026, 5, 1),
                    LocalDate.of(2026, 5, 31),
                    new BigDecimal("100000"),
                    null, null, null, null,
                    10, null
                );

                SetRateAndInventoryCommand command = RateAndInventoryApiMapper.toCommand(1L, request);

                assertThat(command.overrides()).isEmpty();
            }

            @Test
            @DisplayName("override 항목의 date, price, reason이 그대로 전달된다")
            void shouldMapOverrideItems() {
                var request = new SetRateAndInventoryApiRequest(
                    LocalDate.of(2026, 5, 1),
                    LocalDate.of(2026, 5, 31),
                    new BigDecimal("100000"),
                    null, null, null, null,
                    10,
                    List.of(
                        new SetRateAndInventoryApiRequest.OverrideItem(
                            LocalDate.of(2026, 5, 5),
                            new BigDecimal("150000"),
                            "어린이날 특가"
                        ),
                        new SetRateAndInventoryApiRequest.OverrideItem(
                            LocalDate.of(2026, 5, 15),
                            new BigDecimal("80000"),
                            null
                        )
                    )
                );

                SetRateAndInventoryCommand command = RateAndInventoryApiMapper.toCommand(1L, request);

                assertThat(command.overrides()).hasSize(2);

                var first = command.overrides().get(0);
                assertThat(first.date()).isEqualTo(LocalDate.of(2026, 5, 5));
                assertThat(first.price()).isEqualByComparingTo(new BigDecimal("150000"));
                assertThat(first.reason()).isEqualTo("어린이날 특가");

                var second = command.overrides().get(1);
                assertThat(second.date()).isEqualTo(LocalDate.of(2026, 5, 15));
                assertThat(second.price()).isEqualByComparingTo(new BigDecimal("80000"));
                assertThat(second.reason()).isNull();
            }
        }

        @Test
        @DisplayName("ratePlanId가 RatePlanId VO로 정확하게 변환된다")
        void shouldConvertRatePlanIdToVo() {
            var request = new SetRateAndInventoryApiRequest(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                new BigDecimal("100000"),
                null, null, null, null,
                5, null
            );

            SetRateAndInventoryCommand command = RateAndInventoryApiMapper.toCommand(42L, request);

            assertThat(command.ratePlanId().value()).isEqualTo(42L);
        }
    }
}
