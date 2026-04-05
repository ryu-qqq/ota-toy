package com.ryuqq.otatoy.domain.reservation;

import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.inventory.InventoryId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationItemEqualityTest {

    private static final Instant NOW = Instant.parse("2026-04-04T00:00:00Z");
    private static final Money NIGHTLY_RATE = Money.of(100_000);

    @Nested
    @DisplayName("T-6: ReservationItem 동등성 검증")
    class Equality {

        @Test
        @DisplayName("같은 ID를 가진 ReservationItem은 동등하다")
        void shouldBeEqualWithSameId() {
            ReservationItem item1 = ReservationItem.reconstitute(
                    ReservationItemId.of(10L),
                    InventoryId.of(100L), LocalDate.of(2026, 4, 10), NIGHTLY_RATE, NOW, NOW
            );
            ReservationItem item2 = ReservationItem.reconstitute(
                    ReservationItemId.of(10L),
                    InventoryId.of(200L), LocalDate.of(2026, 4, 11), NIGHTLY_RATE, NOW, NOW
            );

            assertThat(item1).isEqualTo(item2);
            assertThat(item1.hashCode()).isEqualTo(item2.hashCode());
        }

        @Test
        @DisplayName("다른 ID를 가진 ReservationItem은 동등하지 않다")
        void shouldNotBeEqualWithDifferentId() {
            ReservationItem item1 = ReservationItem.reconstitute(
                    ReservationItemId.of(10L),
                    InventoryId.of(100L), LocalDate.of(2026, 4, 10), NIGHTLY_RATE, NOW, NOW
            );
            ReservationItem item2 = ReservationItem.reconstitute(
                    ReservationItemId.of(20L),
                    InventoryId.of(100L), LocalDate.of(2026, 4, 10), NIGHTLY_RATE, NOW, NOW
            );

            assertThat(item1).isNotEqualTo(item2);
        }

        @Test
        @DisplayName("id가 null인 ReservationItem은 동등하지 않다")
        void shouldNotBeEqualWhenIdIsNull() {
            ReservationItem item1 = ReservationItem.forNew(InventoryId.of(100L), LocalDate.of(2026, 4, 10), NIGHTLY_RATE, NOW);
            ReservationItem item2 = ReservationItem.forNew(InventoryId.of(100L), LocalDate.of(2026, 4, 10), NIGHTLY_RATE, NOW);

            assertThat(item1.id()).isNotNull();
            assertThat(item1.id().value()).isNull();
        }
    }
}
