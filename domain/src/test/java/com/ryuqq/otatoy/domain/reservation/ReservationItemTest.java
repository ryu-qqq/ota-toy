package com.ryuqq.otatoy.domain.reservation;

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
    private static final ReservationId RESERVATION_ID = ReservationId.of(1L);
    private static final Instant NOW = Instant.parse("2026-04-04T00:00:00Z");

    @Nested
    @DisplayName("T-1: 생성 검증 — forNew()")
    class Creation {

        @Test
        @DisplayName("정상 생성 시 id는 새 ID이고 필드가 정상 할당된다")
        void shouldCreateSuccessfully() {
            ReservationItem item = ReservationItem.forNew(RESERVATION_ID, INVENTORY_ID, STAY_DATE, NOW);

            assertThat(item.id()).isNotNull();
            assertThat(item.id().isNew()).isTrue();
            assertThat(item.reservationId()).isEqualTo(RESERVATION_ID);
            assertThat(item.inventoryId()).isEqualTo(INVENTORY_ID);
            assertThat(item.stayDate()).isEqualTo(STAY_DATE);
            assertThat(item.createdAt()).isEqualTo(NOW);
            assertThat(item.updatedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("reservationId가 null이어도 생성 성공 (아직 저장 전)")
        void shouldAllowNullReservationId() {
            ReservationItem item = ReservationItem.forNew(null, INVENTORY_ID, STAY_DATE, NOW);

            assertThat(item.reservationId()).isNull();
        }

        @Test
        @DisplayName("stayDate가 null이면 생성 실패")
        void shouldFailWhenStayDateIsNull() {
            assertThatThrownBy(() -> ReservationItem.forNew(RESERVATION_ID, INVENTORY_ID, null, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("숙박 날짜는 필수");
        }

        @Test
        @DisplayName("inventoryId가 null이면 생성 실패")
        void shouldFailWhenInventoryIdIsNull() {
            assertThatThrownBy(() -> ReservationItem.forNew(RESERVATION_ID, null, STAY_DATE, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("재고 ID는 필수");
        }
    }

    @Nested
    @DisplayName("T-3: reconstitute — DB 복원")
    class Reconstitute {

        @Test
        @DisplayName("모든 필드가 그대로 복원된다")
        void shouldReconstituteFaithfully() {
            ReservationItem item = ReservationItem.reconstitute(ReservationItemId.of(10L), ReservationId.of(5L), INVENTORY_ID, STAY_DATE, NOW, NOW);

            assertThat(item.id()).isEqualTo(ReservationItemId.of(10L));
            assertThat(item.reservationId()).isEqualTo(ReservationId.of(5L));
            assertThat(item.inventoryId()).isEqualTo(INVENTORY_ID);
            assertThat(item.stayDate()).isEqualTo(STAY_DATE);
            assertThat(item.createdAt()).isEqualTo(NOW);
            assertThat(item.updatedAt()).isEqualTo(NOW);
        }
    }
}
