package com.ryuqq.otatoy.domain.reservation;

import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.inventory.InventoryId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationItemTest {

    private static final InventoryId INVENTORY_ID = InventoryId.of(100L);
    private static final LocalDate STAY_DATE = LocalDate.of(2026, 4, 10);
    private static final Money NIGHTLY_RATE = Money.of(100_000);
    private static final Instant NOW = Instant.parse("2026-04-04T00:00:00Z");

    @Nested
    @DisplayName("T-1: 생성 검증 — forNew()")
    class Creation {

        @Test
        @DisplayName("정상 생성 시 id는 새 ID이고 필드가 정상 할당된다")
        void shouldCreateSuccessfully() {
            ReservationItem item = ReservationItem.forNew(INVENTORY_ID, STAY_DATE, NIGHTLY_RATE, NOW);

            assertThat(item.id()).isNotNull();
            assertThat(item.id().isNew()).isTrue();
            assertThat(item.inventoryId()).isEqualTo(INVENTORY_ID);
            assertThat(item.stayDate()).isEqualTo(STAY_DATE);
            assertThat(item.nightlyRate()).isEqualTo(NIGHTLY_RATE);
            assertThat(item.createdAt()).isEqualTo(NOW);
            assertThat(item.updatedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("stayDate가 null이면 생성 실패")
        void shouldFailWhenStayDateIsNull() {
            assertThatThrownBy(() -> ReservationItem.forNew(INVENTORY_ID, null, NIGHTLY_RATE, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("숙박 날짜는 필수");
        }

        @Test
        @DisplayName("inventoryId가 null이면 생성 실패")
        void shouldFailWhenInventoryIdIsNull() {
            assertThatThrownBy(() -> ReservationItem.forNew(null, STAY_DATE, NIGHTLY_RATE, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("재고 ID는 필수");
        }

        @Test
        @DisplayName("nightlyRate가 null이면 생성 실패")
        void shouldFailWhenNightlyRateIsNull() {
            assertThatThrownBy(() -> ReservationItem.forNew(INVENTORY_ID, STAY_DATE, null, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("1박 요금은 필수");
        }
    }

    @Nested
    @DisplayName("T-3: reconstitute — DB 복원")
    class Reconstitute {

        @Test
        @DisplayName("모든 필드가 그대로 복원된다")
        void shouldReconstituteFaithfully() {
            ReservationItem item = ReservationItem.reconstitute(
                    ReservationItemId.of(10L), INVENTORY_ID,
                    STAY_DATE, NIGHTLY_RATE, NOW, NOW
            );

            assertThat(item.id()).isEqualTo(ReservationItemId.of(10L));
            assertThat(item.inventoryId()).isEqualTo(INVENTORY_ID);
            assertThat(item.stayDate()).isEqualTo(STAY_DATE);
            assertThat(item.nightlyRate()).isEqualTo(NIGHTLY_RATE);
            assertThat(item.createdAt()).isEqualTo(NOW);
            assertThat(item.updatedAt()).isEqualTo(NOW);
        }
    }
}
