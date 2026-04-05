package com.ryuqq.otatoy.domain.reservation;

import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.inventory.InventoryId;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationLineTest {

    private static final Instant NOW = Instant.parse("2026-04-04T00:00:00Z");
    private static final RatePlanId RATE_PLAN_ID = RatePlanId.of(1L);
    private static final Money NIGHTLY_RATE = Money.of(100_000);
    private static final Money SUBTOTAL_AMOUNT = Money.of(200_000);

    private static List<ReservationItem> defaultItems() {
        return List.of(
                ReservationItem.forNew(InventoryId.of(100L), LocalDate.of(2026, 4, 10), NIGHTLY_RATE, NOW),
                ReservationItem.forNew(InventoryId.of(101L), LocalDate.of(2026, 4, 11), NIGHTLY_RATE, NOW)
        );
    }

    @Nested
    @DisplayName("T-1: 생성 검증 — forNew()")
    class Creation {

        @Test
        @DisplayName("정상 생성 시 id는 새 ID이고 필드가 정상 할당된다")
        void shouldCreateSuccessfully() {
            ReservationLine line = ReservationLine.forNew(null, RATE_PLAN_ID, 1,
                    SUBTOTAL_AMOUNT, defaultItems(), NOW);

            assertThat(line.id()).isNotNull();
            assertThat(line.id().isNew()).isTrue();
            assertThat(line.reservationId()).isNull();
            assertThat(line.ratePlanId()).isEqualTo(RATE_PLAN_ID);
            assertThat(line.roomCount()).isEqualTo(1);
            assertThat(line.subtotalAmount()).isEqualTo(SUBTOTAL_AMOUNT);
            assertThat(line.items()).hasSize(2);
            assertThat(line.createdAt()).isEqualTo(NOW);
            assertThat(line.updatedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("다객실 예약 — roomCount가 2 이상이면 성공")
        void shouldCreateWithMultipleRooms() {
            ReservationLine line = ReservationLine.forNew(null, RATE_PLAN_ID, 3,
                    Money.of(600_000), defaultItems(), NOW);

            assertThat(line.roomCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("ratePlanId가 null이면 생성 실패")
        void shouldFailWhenRatePlanIdIsNull() {
            assertThatThrownBy(() -> ReservationLine.forNew(null, null, 1,
                    SUBTOTAL_AMOUNT, defaultItems(), NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("요금 정책 ID는 필수");
        }

        @Test
        @DisplayName("roomCount가 0이면 생성 실패")
        void shouldFailWhenRoomCountIsZero() {
            assertThatThrownBy(() -> ReservationLine.forNew(null, RATE_PLAN_ID, 0,
                    SUBTOTAL_AMOUNT, defaultItems(), NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("객실 수는 1개 이상");
        }

        @Test
        @DisplayName("roomCount가 음수면 생성 실패")
        void shouldFailWhenRoomCountIsNegative() {
            assertThatThrownBy(() -> ReservationLine.forNew(null, RATE_PLAN_ID, -1,
                    SUBTOTAL_AMOUNT, defaultItems(), NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("객실 수는 1개 이상");
        }

        @Test
        @DisplayName("subtotalAmount가 null이면 생성 실패")
        void shouldFailWhenSubtotalAmountIsNull() {
            assertThatThrownBy(() -> ReservationLine.forNew(null, RATE_PLAN_ID, 1,
                    null, defaultItems(), NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("소계 금액은 필수");
        }

        @Test
        @DisplayName("items가 null이면 생성 실패")
        void shouldFailWhenItemsIsNull() {
            assertThatThrownBy(() -> ReservationLine.forNew(null, RATE_PLAN_ID, 1,
                    SUBTOTAL_AMOUNT, null, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("예약 항목은 최소 1개 이상");
        }

        @Test
        @DisplayName("items가 빈 리스트이면 생성 실패")
        void shouldFailWhenItemsIsEmpty() {
            assertThatThrownBy(() -> ReservationLine.forNew(null, RATE_PLAN_ID, 1,
                    SUBTOTAL_AMOUNT, List.of(), NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("예약 항목은 최소 1개 이상");
        }

        @Test
        @DisplayName("items 리스트는 불변 복사본으로 저장된다")
        void shouldStoreItemsAsUnmodifiableCopy() {
            ReservationLine line = ReservationLine.forNew(null, RATE_PLAN_ID, 1,
                    SUBTOTAL_AMOUNT, defaultItems(), NOW);

            assertThatThrownBy(() -> line.items().add(
                    ReservationItem.forNew(InventoryId.of(999L), LocalDate.of(2026, 4, 12), NIGHTLY_RATE, NOW)
            ))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("T-3: reconstitute — DB 복원")
    class Reconstitute {

        @Test
        @DisplayName("모든 필드가 그대로 복원된다")
        void shouldReconstituteFaithfully() {
            ReservationLine line = ReservationLine.reconstitute(
                    ReservationLineId.of(10L), ReservationId.of(5L),
                    RATE_PLAN_ID, 2, Money.of(400_000), NOW, NOW, defaultItems()
            );

            assertThat(line.id()).isEqualTo(ReservationLineId.of(10L));
            assertThat(line.reservationId()).isEqualTo(ReservationId.of(5L));
            assertThat(line.ratePlanId()).isEqualTo(RATE_PLAN_ID);
            assertThat(line.roomCount()).isEqualTo(2);
            assertThat(line.subtotalAmount()).isEqualTo(Money.of(400_000));
            assertThat(line.items()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("T-6: 동등성 검증")
    class Equality {

        @Test
        @DisplayName("같은 ID를 가진 ReservationLine은 동등하다")
        void shouldBeEqualWithSameId() {
            ReservationLine line1 = ReservationLine.reconstitute(
                    ReservationLineId.of(10L), ReservationId.of(1L),
                    RATE_PLAN_ID, 1, SUBTOTAL_AMOUNT, NOW, NOW, defaultItems()
            );
            ReservationLine line2 = ReservationLine.reconstitute(
                    ReservationLineId.of(10L), ReservationId.of(2L),
                    RatePlanId.of(99L), 3, Money.of(999_000), NOW, NOW, defaultItems()
            );

            assertThat(line1).isEqualTo(line2);
            assertThat(line1.hashCode()).isEqualTo(line2.hashCode());
        }

        @Test
        @DisplayName("다른 ID를 가진 ReservationLine은 동등하지 않다")
        void shouldNotBeEqualWithDifferentId() {
            ReservationLine line1 = ReservationLine.reconstitute(
                    ReservationLineId.of(10L), ReservationId.of(1L),
                    RATE_PLAN_ID, 1, SUBTOTAL_AMOUNT, NOW, NOW, defaultItems()
            );
            ReservationLine line2 = ReservationLine.reconstitute(
                    ReservationLineId.of(20L), ReservationId.of(1L),
                    RATE_PLAN_ID, 1, SUBTOTAL_AMOUNT, NOW, NOW, defaultItems()
            );

            assertThat(line1).isNotEqualTo(line2);
        }
    }
}
