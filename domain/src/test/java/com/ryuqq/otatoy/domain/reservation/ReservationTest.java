package com.ryuqq.otatoy.domain.reservation;

import com.ryuqq.otatoy.domain.common.vo.DateRange;
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

class ReservationTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 4, 4);
    private static final Instant NOW = Instant.parse("2026-04-04T00:00:00Z");

    private static final RatePlanId RATE_PLAN_ID = RatePlanId.of(1L);
    private static final ReservationNo RESERVATION_NO = ReservationNo.of("RSV-20260404-001");
    private static final GuestInfo GUEST_INFO = GuestInfo.of("홍길동", "010-1234-5678", "hong@test.com");
    private static final DateRange STAY_PERIOD = new DateRange(LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 12));
    private static final int GUEST_COUNT = 2;
    private static final Money TOTAL_AMOUNT = Money.of(200_000);
    private static final String BOOKING_SNAPSHOT = "{\"roomType\":\"deluxe\"}";

    private static List<ReservationItem> defaultItems() {
        return List.of(
                ReservationItem.forNew(null, InventoryId.of(100L), LocalDate.of(2026, 4, 10), NOW),
                ReservationItem.forNew(null, InventoryId.of(101L), LocalDate.of(2026, 4, 11), NOW)
        );
    }

    private Reservation createDefaultReservation() {
        return Reservation.forNew(
                RATE_PLAN_ID, RESERVATION_NO, GUEST_INFO, STAY_PERIOD,
                GUEST_COUNT, TOTAL_AMOUNT, BOOKING_SNAPSHOT, defaultItems(),
                TODAY, NOW
        );
    }

    private Reservation createReservationWithStatus(ReservationStatus status) {
        return Reservation.reconstitute(
                ReservationId.of(1L), RATE_PLAN_ID, RESERVATION_NO, GUEST_INFO,
                STAY_PERIOD, GUEST_COUNT, TOTAL_AMOUNT, status, null,
                BOOKING_SNAPSHOT, NOW, NOW, null, defaultItems()
        );
    }

    @Nested
    @DisplayName("T-1: 생성 검증 — forNew()")
    class Creation {

        @Test
        @DisplayName("정상 생성 시 PENDING 상태이고 id는 null이다")
        void shouldCreateWithPendingStatusAndNullId() {
            Reservation reservation = createDefaultReservation();

            assertThat(reservation.id()).isNull();
            assertThat(reservation.status()).isEqualTo(ReservationStatus.PENDING);
            assertThat(reservation.ratePlanId()).isEqualTo(RATE_PLAN_ID);
            assertThat(reservation.reservationNo()).isEqualTo(RESERVATION_NO);
            assertThat(reservation.guestInfo()).isEqualTo(GUEST_INFO);
            assertThat(reservation.stayPeriod()).isEqualTo(STAY_PERIOD);
            assertThat(reservation.guestCount()).isEqualTo(GUEST_COUNT);
            assertThat(reservation.totalAmount()).isEqualTo(TOTAL_AMOUNT);
            assertThat(reservation.bookingSnapshot()).isEqualTo(BOOKING_SNAPSHOT);
            assertThat(reservation.createdAt()).isEqualTo(NOW);
            assertThat(reservation.updatedAt()).isEqualTo(NOW);
            assertThat(reservation.cancelReason()).isNull();
            assertThat(reservation.cancelledAt()).isNull();
            assertThat(reservation.items()).hasSize(2);
        }

        @Test
        @DisplayName("ratePlanId가 null이면 생성 실패")
        void shouldFailWhenRatePlanIdIsNull() {
            assertThatThrownBy(() -> Reservation.forNew(
                    null, RESERVATION_NO, GUEST_INFO, STAY_PERIOD,
                    GUEST_COUNT, TOTAL_AMOUNT, BOOKING_SNAPSHOT, defaultItems(),
                    TODAY, NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("요금 정책 ID는 필수");
        }

        @Test
        @DisplayName("reservationNo가 null이면 생성 실패")
        void shouldFailWhenReservationNoIsNull() {
            assertThatThrownBy(() -> ReservationNo.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("예약 번호는 필수");
        }

        @Test
        @DisplayName("reservationNo가 빈 문자열이면 생성 실패")
        void shouldFailWhenReservationNoIsBlank() {
            assertThatThrownBy(() -> ReservationNo.of("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("예약 번호는 필수");
        }

        @Test
        @DisplayName("guestInfo가 null이면 생성 실패")
        void shouldFailWhenGuestInfoIsNull() {
            assertThatThrownBy(() -> Reservation.forNew(
                    RATE_PLAN_ID, RESERVATION_NO, null, STAY_PERIOD,
                    GUEST_COUNT, TOTAL_AMOUNT, BOOKING_SNAPSHOT, defaultItems(),
                    TODAY, NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("투숙객 정보는 필수");
        }

        @Test
        @DisplayName("stayPeriod가 null이면 생성 실패")
        void shouldFailWhenStayPeriodIsNull() {
            assertThatThrownBy(() -> Reservation.forNew(
                    RATE_PLAN_ID, RESERVATION_NO, GUEST_INFO, null,
                    GUEST_COUNT, TOTAL_AMOUNT, BOOKING_SNAPSHOT, defaultItems(),
                    TODAY, NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("숙박 기간은 필수");
        }

        @Test
        @DisplayName("guestCount가 0이면 생성 실패")
        void shouldFailWhenGuestCountIsZero() {
            assertThatThrownBy(() -> Reservation.forNew(
                    RATE_PLAN_ID, RESERVATION_NO, GUEST_INFO, STAY_PERIOD,
                    0, TOTAL_AMOUNT, BOOKING_SNAPSHOT, defaultItems(),
                    TODAY, NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("투숙 인원은 1명 이상");
        }

        @Test
        @DisplayName("guestCount가 음수면 생성 실패")
        void shouldFailWhenGuestCountIsNegative() {
            assertThatThrownBy(() -> Reservation.forNew(
                    RATE_PLAN_ID, RESERVATION_NO, GUEST_INFO, STAY_PERIOD,
                    -1, TOTAL_AMOUNT, BOOKING_SNAPSHOT, defaultItems(),
                    TODAY, NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("투숙 인원은 1명 이상");
        }

        @Test
        @DisplayName("totalAmount가 null이면 생성 실패")
        void shouldFailWhenTotalAmountIsNull() {
            assertThatThrownBy(() -> Reservation.forNew(
                    RATE_PLAN_ID, RESERVATION_NO, GUEST_INFO, STAY_PERIOD,
                    GUEST_COUNT, null, BOOKING_SNAPSHOT, defaultItems(),
                    TODAY, NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("총 금액은 필수");
        }

        @Test
        @DisplayName("체크인 날짜가 과거이면 생성 실패")
        void shouldFailWhenCheckInDateIsInThePast() {
            DateRange pastPeriod = new DateRange(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 3));
            List<ReservationItem> items = List.of(
                    ReservationItem.forNew(null, InventoryId.of(100L), LocalDate.of(2026, 4, 1), NOW),
                    ReservationItem.forNew(null, InventoryId.of(101L), LocalDate.of(2026, 4, 2), NOW)
            );

            assertThatThrownBy(() -> Reservation.forNew(
                    RATE_PLAN_ID, RESERVATION_NO, GUEST_INFO, pastPeriod,
                    GUEST_COUNT, TOTAL_AMOUNT, BOOKING_SNAPSHOT, items,
                    TODAY, NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("과거 날짜는 예약할 수 없습니다");
        }

        @Test
        @DisplayName("당일 예약(체크인 == today)은 성공한다")
        void shouldSucceedWhenCheckInIsToday() {
            DateRange todayPeriod = new DateRange(TODAY, TODAY.plusDays(1));
            List<ReservationItem> items = List.of(
                    ReservationItem.forNew(null, InventoryId.of(100L), TODAY, NOW)
            );

            Reservation reservation = Reservation.forNew(
                    RATE_PLAN_ID, RESERVATION_NO, GUEST_INFO, todayPeriod,
                    GUEST_COUNT, TOTAL_AMOUNT, BOOKING_SNAPSHOT, items,
                    TODAY, NOW
            );

            assertThat(reservation.status()).isEqualTo(ReservationStatus.PENDING);
        }

        @Test
        @DisplayName("30박 초과 숙박은 생성 실패")
        void shouldFailWhenStayExceeds30Nights() {
            DateRange longPeriod = new DateRange(TODAY.plusDays(1), TODAY.plusDays(32));
            List<ReservationItem> items = List.of(
                    ReservationItem.forNew(null, InventoryId.of(100L), TODAY.plusDays(1), NOW)
            );

            assertThatThrownBy(() -> Reservation.forNew(
                    RATE_PLAN_ID, RESERVATION_NO, GUEST_INFO, longPeriod,
                    GUEST_COUNT, TOTAL_AMOUNT, BOOKING_SNAPSHOT, items,
                    TODAY, NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("최대 30박까지 예약 가능합니다");
        }

        @Test
        @DisplayName("정확히 30박 숙박은 성공한다")
        void shouldSucceedWhenStayIsExactly30Nights() {
            DateRange exactPeriod = new DateRange(TODAY.plusDays(1), TODAY.plusDays(31));
            List<ReservationItem> items = List.of(
                    ReservationItem.forNew(null, InventoryId.of(100L), TODAY.plusDays(1), NOW)
            );

            Reservation reservation = Reservation.forNew(
                    RATE_PLAN_ID, RESERVATION_NO, GUEST_INFO, exactPeriod,
                    GUEST_COUNT, TOTAL_AMOUNT, BOOKING_SNAPSHOT, items,
                    TODAY, NOW
            );

            assertThat(reservation.stayPeriod().nights()).isEqualTo(30);
        }

        @Test
        @DisplayName("items가 null이면 생성 실패")
        void shouldFailWhenItemsIsNull() {
            assertThatThrownBy(() -> Reservation.forNew(
                    RATE_PLAN_ID, RESERVATION_NO, GUEST_INFO, STAY_PERIOD,
                    GUEST_COUNT, TOTAL_AMOUNT, BOOKING_SNAPSHOT, null,
                    TODAY, NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("예약 항목은 최소 1개 이상");
        }

        @Test
        @DisplayName("items가 빈 리스트이면 생성 실패")
        void shouldFailWhenItemsIsEmpty() {
            assertThatThrownBy(() -> Reservation.forNew(
                    RATE_PLAN_ID, RESERVATION_NO, GUEST_INFO, STAY_PERIOD,
                    GUEST_COUNT, TOTAL_AMOUNT, BOOKING_SNAPSHOT, List.of(),
                    TODAY, NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("예약 항목은 최소 1개 이상");
        }

        @Test
        @DisplayName("items 리스트는 불변 복사본으로 저장된다")
        void shouldStoreItemsAsUnmodifiableCopy() {
            Reservation reservation = createDefaultReservation();

            assertThatThrownBy(() -> reservation.items().add(
                    ReservationItem.forNew(null, InventoryId.of(999L), LocalDate.of(2026, 4, 12), NOW)
            ))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("T-2: 상태 전이 검증")
    class StateTransition {

        @Nested
        @DisplayName("confirm()")
        class Confirm {

            @Test
            @DisplayName("PENDING → CONFIRMED 전이 성공")
            void shouldConfirmFromPending() {
                Reservation reservation = createReservationWithStatus(ReservationStatus.PENDING);

                reservation.confirm(NOW);

                assertThat(reservation.status()).isEqualTo(ReservationStatus.CONFIRMED);
            }

            @Test
            @DisplayName("CONFIRMED 상태에서 confirm() 호출 시 InvalidReservationStateException")
            void shouldFailWhenAlreadyConfirmed() {
                Reservation reservation = createReservationWithStatus(ReservationStatus.CONFIRMED);

                assertThatThrownBy(() -> reservation.confirm(NOW))
                        .isInstanceOf(InvalidReservationStateException.class);
            }

            @Test
            @DisplayName("COMPLETED 상태에서 confirm() 호출 시 InvalidReservationStateException")
            void shouldFailWhenCompleted() {
                Reservation reservation = createReservationWithStatus(ReservationStatus.COMPLETED);

                assertThatThrownBy(() -> reservation.confirm(NOW))
                        .isInstanceOf(InvalidReservationStateException.class);
            }

            @Test
            @DisplayName("CANCELLED 상태에서 confirm() 호출 시 InvalidReservationStateException")
            void shouldFailWhenCancelled() {
                Reservation reservation = createReservationWithStatus(ReservationStatus.CANCELLED);

                assertThatThrownBy(() -> reservation.confirm(NOW))
                        .isInstanceOf(InvalidReservationStateException.class);
            }
        }

        @Nested
        @DisplayName("cancel()")
        class Cancel {

            @Test
            @DisplayName("PENDING → CANCELLED 전이 성공, cancelReason과 cancelledAt 설정")
            void shouldCancelFromPending() {
                Reservation reservation = createReservationWithStatus(ReservationStatus.PENDING);
                Instant cancelTime = Instant.parse("2026-04-05T10:00:00Z");

                reservation.cancel("고객 요청 취소", cancelTime);

                assertThat(reservation.status()).isEqualTo(ReservationStatus.CANCELLED);
                assertThat(reservation.cancelReason()).isEqualTo("고객 요청 취소");
                assertThat(reservation.cancelledAt()).isEqualTo(cancelTime);
            }

            @Test
            @DisplayName("CONFIRMED → CANCELLED 전이 성공")
            void shouldCancelFromConfirmed() {
                Reservation reservation = createReservationWithStatus(ReservationStatus.CONFIRMED);
                Instant cancelTime = Instant.parse("2026-04-05T10:00:00Z");

                reservation.cancel("일정 변경", cancelTime);

                assertThat(reservation.status()).isEqualTo(ReservationStatus.CANCELLED);
                assertThat(reservation.cancelReason()).isEqualTo("일정 변경");
                assertThat(reservation.cancelledAt()).isEqualTo(cancelTime);
            }

            @Test
            @DisplayName("이미 취소된 예약을 취소하면 ReservationAlreadyCancelledException (RSV-003)")
            void shouldFailWhenAlreadyCancelled() {
                Reservation reservation = createReservationWithStatus(ReservationStatus.CANCELLED);

                assertThatThrownBy(() -> reservation.cancel("중복 취소", NOW))
                        .isInstanceOf(ReservationAlreadyCancelledException.class)
                        .satisfies(ex -> {
                            ReservationAlreadyCancelledException e = (ReservationAlreadyCancelledException) ex;
                            assertThat(e.getErrorCode().getCode()).isEqualTo("RSV-003");
                        });
            }

            @Test
            @DisplayName("이미 완료된 예약을 취소하면 ReservationAlreadyCompletedException (RSV-004)")
            void shouldFailWhenAlreadyCompleted() {
                Reservation reservation = createReservationWithStatus(ReservationStatus.COMPLETED);

                assertThatThrownBy(() -> reservation.cancel("완료 후 취소", NOW))
                        .isInstanceOf(ReservationAlreadyCompletedException.class)
                        .satisfies(ex -> {
                            ReservationAlreadyCompletedException e = (ReservationAlreadyCompletedException) ex;
                            assertThat(e.getErrorCode().getCode()).isEqualTo("RSV-004");
                        });
            }

            @Test
            @DisplayName("NO_SHOW 상태에서 cancel() 호출 시 InvalidReservationStateException")
            void shouldFailWhenNoShow() {
                Reservation reservation = createReservationWithStatus(ReservationStatus.NO_SHOW);

                assertThatThrownBy(() -> reservation.cancel("노쇼 후 취소", NOW))
                        .isInstanceOf(InvalidReservationStateException.class)
                        .satisfies(ex -> {
                            InvalidReservationStateException e = (InvalidReservationStateException) ex;
                            assertThat(e.getErrorCode().getCode()).isEqualTo("RSV-002");
                        });
            }
        }

        @Nested
        @DisplayName("complete()")
        class Complete {

            @Test
            @DisplayName("CONFIRMED → COMPLETED 전이 성공")
            void shouldCompleteFromConfirmed() {
                Reservation reservation = createReservationWithStatus(ReservationStatus.CONFIRMED);

                reservation.complete(NOW);

                assertThat(reservation.status()).isEqualTo(ReservationStatus.COMPLETED);
            }

            @Test
            @DisplayName("PENDING 상태에서 complete() 호출 시 InvalidReservationStateException")
            void shouldFailWhenPending() {
                Reservation reservation = createReservationWithStatus(ReservationStatus.PENDING);

                assertThatThrownBy(() -> reservation.complete(NOW))
                        .isInstanceOf(InvalidReservationStateException.class);
            }

            @Test
            @DisplayName("CANCELLED 상태에서 complete() 호출 시 InvalidReservationStateException")
            void shouldFailWhenCancelled() {
                Reservation reservation = createReservationWithStatus(ReservationStatus.CANCELLED);

                assertThatThrownBy(() -> reservation.complete(NOW))
                        .isInstanceOf(InvalidReservationStateException.class);
            }
        }

        @Nested
        @DisplayName("noShow()")
        class NoShow {

            @Test
            @DisplayName("CONFIRMED → NO_SHOW 전이 성공")
            void shouldNoShowFromConfirmed() {
                Reservation reservation = createReservationWithStatus(ReservationStatus.CONFIRMED);

                reservation.noShow(NOW);

                assertThat(reservation.status()).isEqualTo(ReservationStatus.NO_SHOW);
            }

            @Test
            @DisplayName("PENDING 상태에서 noShow() 호출 시 InvalidReservationStateException")
            void shouldFailWhenPending() {
                Reservation reservation = createReservationWithStatus(ReservationStatus.PENDING);

                assertThatThrownBy(() -> reservation.noShow(NOW))
                        .isInstanceOf(InvalidReservationStateException.class);
            }

            @Test
            @DisplayName("COMPLETED 상태에서 noShow() 호출 시 InvalidReservationStateException")
            void shouldFailWhenCompleted() {
                Reservation reservation = createReservationWithStatus(ReservationStatus.COMPLETED);

                assertThatThrownBy(() -> reservation.noShow(NOW))
                        .isInstanceOf(InvalidReservationStateException.class);
            }

            @Test
            @DisplayName("CANCELLED 상태에서 noShow() 호출 시 InvalidReservationStateException")
            void shouldFailWhenCancelled() {
                Reservation reservation = createReservationWithStatus(ReservationStatus.CANCELLED);

                assertThatThrownBy(() -> reservation.noShow(NOW))
                        .isInstanceOf(InvalidReservationStateException.class);
            }
        }
    }

    @Nested
    @DisplayName("T-3: reconstitute — DB 복원")
    class Reconstitute {

        @Test
        @DisplayName("모든 필드가 검증 없이 그대로 복원된다")
        void shouldReconstituteFaithfully() {
            ReservationId id = ReservationId.of(42L);
            Instant createdAt = Instant.parse("2026-04-01T00:00:00Z");
            Instant updatedAt = Instant.parse("2026-04-03T10:00:00Z");
            Instant cancelledAt = Instant.parse("2026-04-03T12:00:00Z");

            Reservation reservation = Reservation.reconstitute(
                    id, RATE_PLAN_ID, RESERVATION_NO, GUEST_INFO, STAY_PERIOD,
                    GUEST_COUNT, TOTAL_AMOUNT, ReservationStatus.CANCELLED, "환불 요청",
                    BOOKING_SNAPSHOT, createdAt, updatedAt, cancelledAt, defaultItems()
            );

            assertThat(reservation.id()).isEqualTo(id);
            assertThat(reservation.ratePlanId()).isEqualTo(RATE_PLAN_ID);
            assertThat(reservation.reservationNo()).isEqualTo(RESERVATION_NO);
            assertThat(reservation.guestInfo()).isEqualTo(GUEST_INFO);
            assertThat(reservation.stayPeriod()).isEqualTo(STAY_PERIOD);
            assertThat(reservation.guestCount()).isEqualTo(GUEST_COUNT);
            assertThat(reservation.totalAmount()).isEqualTo(TOTAL_AMOUNT);
            assertThat(reservation.status()).isEqualTo(ReservationStatus.CANCELLED);
            assertThat(reservation.cancelReason()).isEqualTo("환불 요청");
            assertThat(reservation.bookingSnapshot()).isEqualTo(BOOKING_SNAPSHOT);
            assertThat(reservation.createdAt()).isEqualTo(createdAt);
            assertThat(reservation.updatedAt()).isEqualTo(updatedAt);
            assertThat(reservation.cancelledAt()).isEqualTo(cancelledAt);
            assertThat(reservation.items()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("T-6: 동등성 검증")
    class Equality {

        @Test
        @DisplayName("같은 ID를 가진 Reservation은 동등하다")
        void shouldBeEqualWithSameId() {
            Reservation r1 = createReservationWithStatus(ReservationStatus.PENDING);
            Reservation r2 = createReservationWithStatus(ReservationStatus.CONFIRMED);

            assertThat(r1).isEqualTo(r2);
            assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        }

        @Test
        @DisplayName("id가 null인 Reservation은 동등하지 않다")
        void shouldNotBeEqualWhenIdIsNull() {
            Reservation r1 = createDefaultReservation();
            Reservation r2 = createDefaultReservation();

            assertThat(r1).isNotEqualTo(r2);
        }
    }
}
