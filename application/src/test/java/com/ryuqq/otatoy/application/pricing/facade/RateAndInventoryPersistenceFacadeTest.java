package com.ryuqq.otatoy.application.pricing.facade;

import com.ryuqq.otatoy.application.inventory.port.out.InventoryCommandPort;
import com.ryuqq.otatoy.application.pricing.dto.RateAndInventoryBundle;
import com.ryuqq.otatoy.application.pricing.port.out.RateCommandPort;
import com.ryuqq.otatoy.application.pricing.port.out.RateOverrideCommandPort;
import com.ryuqq.otatoy.application.pricing.port.out.RateRuleCommandPort;
import com.ryuqq.otatoy.domain.inventory.Inventory;
import com.ryuqq.otatoy.domain.inventory.InventoryFixture;
import com.ryuqq.otatoy.domain.pricing.PricingFixtures;
import com.ryuqq.otatoy.domain.pricing.Rate;
import com.ryuqq.otatoy.domain.pricing.RateOverride;
import com.ryuqq.otatoy.domain.pricing.RateRule;
import com.ryuqq.otatoy.domain.pricing.RateRuleId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

/**
 * RateAndInventoryPersistenceFacade 단위 테스트.
 * 여러 CommandPort를 올바른 순서로 호출하며,
 * RateOverride에 rateRuleId를 할당하는 번들 패턴을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class RateAndInventoryPersistenceFacadeTest {

    @Mock RateRuleCommandPort rateRuleCommandPort;
    @Mock RateOverrideCommandPort rateOverrideCommandPort;
    @Mock RateCommandPort rateCommandPort;
    @Mock InventoryCommandPort inventoryCommandPort;

    @InjectMocks RateAndInventoryPersistenceFacade facade;

    @Captor ArgumentCaptor<List<RateOverride>> overridesCaptor;

    @Nested
    @DisplayName("정상 흐름 - 전체 컴포넌트 저장")
    class FullPersist {

        @Test
        @DisplayName("RateRule 저장 후 할당된 ID가 RateOverride에 부여된다")
        void shouldAssignRateRuleIdToOverrides() {
            // given
            RateRule rateRule = PricingFixtures.defaultRateRule();
            RateOverride pendingOverride = RateOverride.forPending(
                rateRule.startDate(), rateRule.endDate(),
                LocalDate.of(2026, 4, 5), BigDecimal.valueOf(170_000), "공휴일", PricingFixtures.NOW
            );
            List<Rate> rates = List.of(PricingFixtures.defaultRate(LocalDate.of(2026, 4, 1), BigDecimal.valueOf(100_000)));
            List<Inventory> inventories = List.of(InventoryFixture.defaultInventory());
            var bundle = new RateAndInventoryBundle(rateRule, List.of(pendingOverride), rates, inventories);

            given(rateRuleCommandPort.persist(rateRule)).willReturn(42L);

            // when
            facade.persist(bundle);

            // then
            then(rateOverrideCommandPort).should().persistAll(overridesCaptor.capture());
            List<RateOverride> saved = overridesCaptor.getValue();
            assertThat(saved).hasSize(1);
            assertThat(saved.get(0).rateRuleId()).isEqualTo(RateRuleId.of(42L));
        }

        @Test
        @DisplayName("RateRule -> Override -> Rate -> Inventory 순서로 저장된다")
        void shouldPersistInCorrectOrder() {
            // given
            RateRule rateRule = PricingFixtures.defaultRateRule();
            RateOverride pendingOverride = RateOverride.forPending(
                rateRule.startDate(), rateRule.endDate(),
                LocalDate.of(2026, 4, 5), BigDecimal.valueOf(170_000), "공휴일", PricingFixtures.NOW
            );
            List<Rate> rates = List.of(PricingFixtures.defaultRate(LocalDate.of(2026, 4, 1), BigDecimal.valueOf(100_000)));
            List<Inventory> inventories = List.of(InventoryFixture.defaultInventory());
            var bundle = new RateAndInventoryBundle(rateRule, List.of(pendingOverride), rates, inventories);

            given(rateRuleCommandPort.persist(rateRule)).willReturn(1L);

            // when
            facade.persist(bundle);

            // then
            InOrder inOrder = inOrder(rateRuleCommandPort, rateOverrideCommandPort, rateCommandPort, inventoryCommandPort);
            inOrder.verify(rateRuleCommandPort).persist(rateRule);
            inOrder.verify(rateOverrideCommandPort).persistAll(any());
            inOrder.verify(rateCommandPort).persistAll(rates);
            inOrder.verify(inventoryCommandPort).persistAll(inventories);
        }
    }

    @Nested
    @DisplayName("빈 컬렉션 처리")
    class EmptyCollections {

        @Test
        @DisplayName("오버라이드가 비어있으면 RateOverrideCommandPort를 호출하지 않는다")
        void shouldSkipOverridePersistWhenEmpty() {
            // given
            RateRule rateRule = PricingFixtures.defaultRateRule();
            List<Rate> rates = List.of(PricingFixtures.defaultRate(LocalDate.of(2026, 4, 1), BigDecimal.valueOf(100_000)));
            List<Inventory> inventories = List.of(InventoryFixture.defaultInventory());
            var bundle = new RateAndInventoryBundle(rateRule, List.of(), rates, inventories);

            given(rateRuleCommandPort.persist(rateRule)).willReturn(1L);

            // when
            facade.persist(bundle);

            // then
            then(rateOverrideCommandPort).should(never()).persistAll(any());
        }

        @Test
        @DisplayName("Rate가 비어있으면 RateCommandPort를 호출하지 않는다")
        void shouldSkipRatePersistWhenEmpty() {
            // given
            RateRule rateRule = PricingFixtures.defaultRateRule();
            List<Inventory> inventories = List.of(InventoryFixture.defaultInventory());
            var bundle = new RateAndInventoryBundle(rateRule, List.of(), List.of(), inventories);

            given(rateRuleCommandPort.persist(rateRule)).willReturn(1L);

            // when
            facade.persist(bundle);

            // then
            then(rateCommandPort).should(never()).persistAll(any());
        }

        @Test
        @DisplayName("Inventory가 비어있으면 InventoryCommandPort를 호출하지 않는다")
        void shouldSkipInventoryPersistWhenEmpty() {
            // given
            RateRule rateRule = PricingFixtures.defaultRateRule();
            var bundle = new RateAndInventoryBundle(rateRule, List.of(), List.of(), List.of());

            given(rateRuleCommandPort.persist(rateRule)).willReturn(1L);

            // when
            facade.persist(bundle);

            // then
            then(inventoryCommandPort).should(never()).persistAll(any());
        }
    }
}
