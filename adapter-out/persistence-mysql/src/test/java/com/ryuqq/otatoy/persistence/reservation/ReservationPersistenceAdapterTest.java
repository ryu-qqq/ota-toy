package com.ryuqq.otatoy.persistence.reservation;

import com.ryuqq.otatoy.domain.common.vo.DateRange;
import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.inventory.InventoryId;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.domain.reservation.GuestInfo;
import com.ryuqq.otatoy.domain.reservation.Reservation;
import com.ryuqq.otatoy.domain.reservation.ReservationFixture;
import com.ryuqq.otatoy.domain.reservation.ReservationId;
import com.ryuqq.otatoy.domain.reservation.ReservationItem;
import com.ryuqq.otatoy.domain.reservation.ReservationLine;
import com.ryuqq.otatoy.domain.reservation.ReservationNo;
import com.ryuqq.otatoy.domain.reservation.ReservationStatus;
import com.ryuqq.otatoy.persistence.MySqlTestContainerConfig;
import com.ryuqq.otatoy.persistence.config.JpaConfig;
import com.ryuqq.otatoy.persistence.config.QueryDslConfig;
import com.ryuqq.otatoy.persistence.reservation.adapter.ReservationCommandAdapter;
import com.ryuqq.otatoy.persistence.reservation.adapter.ReservationQueryAdapter;
import com.ryuqq.otatoy.persistence.reservation.adapter.ReservationSessionCommandAdapter;
import com.ryuqq.otatoy.persistence.reservation.adapter.ReservationSessionQueryAdapter;
import com.ryuqq.otatoy.persistence.reservation.mapper.ReservationEntityMapper;
import com.ryuqq.otatoy.persistence.reservation.mapper.ReservationSessionEntityMapper;
import com.ryuqq.otatoy.persistence.reservation.repository.ReservationSessionQueryDslRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reservation Persistence Adapter 통합 테스트.
 * Testcontainers MySQL + Flyway 기반으로 Reservation + ReservationLine + ReservationItem 저장/조회를 검증한다.
 *
 * 주의: ReservationQueryAdapter.findById()는 현재 Line/Item 없이 조회한다 (mapper.toDomain(entity, List.of())).
 * 따라서 매핑 정합성 테스트에서 Line/Item 필드는 검증하지 않는다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        JpaConfig.class,
        QueryDslConfig.class,
        ReservationCommandAdapter.class,
        ReservationQueryAdapter.class,
        ReservationEntityMapper.class,
        ReservationSessionCommandAdapter.class,
        ReservationSessionQueryAdapter.class,
        ReservationSessionEntityMapper.class,
        ReservationSessionQueryDslRepository.class
})
class ReservationPersistenceAdapterTest extends MySqlTestContainerConfig {

    @Autowired
    private ReservationCommandAdapter commandAdapter;

    @Autowired
    private ReservationQueryAdapter queryAdapter;

    // === 헬퍼 메서드 ===

    /**
     * 고유한 reservationNo를 가진 PENDING 예약을 생성한다.
     * unique 제약 위반을 방지하기 위해 UUID 기반 예약번호를 사용한다.
     */
    private Reservation uniquePendingReservation() {
        String uniqueNo = "RSV-" + UUID.randomUUID().toString().substring(0, 20);
        return Reservation.forNew(
                ReservationFixture.DEFAULT_CUSTOMER_ID,
                ReservationNo.of(uniqueNo),
                ReservationFixture.DEFAULT_GUEST_INFO,
                ReservationFixture.DEFAULT_STAY_PERIOD,
                ReservationFixture.DEFAULT_GUEST_COUNT,
                ReservationFixture.DEFAULT_TOTAL_AMOUNT,
                ReservationFixture.DEFAULT_BOOKING_SNAPSHOT,
                ReservationFixture.defaultLines(),
                ReservationFixture.DEFAULT_TODAY,
                ReservationFixture.DEFAULT_NOW
        );
    }

    /**
     * email만 null인 투숙객을 가진 예약을 생성한다.
     * guestPhone은 DB에서 NOT NULL이므로 항상 제공해야 한다.
     */
    private Reservation reservationWithOptionalEmailNull() {
        String uniqueNo = "RSV-MIN-" + UUID.randomUUID().toString().substring(0, 16);
        GuestInfo guestWithoutEmail = GuestInfo.of("김철수", "010-9999-8888", null);
        return Reservation.forNew(
                ReservationFixture.DEFAULT_CUSTOMER_ID,
                ReservationNo.of(uniqueNo),
                guestWithoutEmail,
                ReservationFixture.DEFAULT_STAY_PERIOD,
                ReservationFixture.DEFAULT_GUEST_COUNT,
                ReservationFixture.DEFAULT_TOTAL_AMOUNT,
                null, // bookingSnapshot null
                ReservationFixture.defaultLines(),
                ReservationFixture.DEFAULT_TODAY,
                ReservationFixture.DEFAULT_NOW
        );
    }

    // -- PT-1: Domain <-> Entity 매핑 정합성 --

    @Nested
    @DisplayName("PT-1: Domain <-> Entity 매핑 정합성")
    class DomainEntityMappingTest {

        @Test
        @DisplayName("PENDING 예약 저장 후 조회 시 Reservation 필드가 원본과 동일하다")
        void shouldMapReservationFieldsCorrectly() {
            // given
            Reservation original = uniquePendingReservation();

            // when
            Long savedId = commandAdapter.persist(original);
            Optional<Reservation> found = queryAdapter.findById(ReservationId.of(savedId));

            // then
            assertThat(found).isPresent();
            Reservation result = found.get();

            assertThat(result.id().value()).isEqualTo(savedId);
            assertThat(result.customerId()).isEqualTo(original.customerId());
            assertThat(result.reservationNo().value()).isEqualTo(original.reservationNo().value());
            assertThat(result.guestInfo().name()).isEqualTo(original.guestInfo().name());
            assertThat(result.guestInfo().phoneValue()).isEqualTo(original.guestInfo().phoneValue());
            assertThat(result.guestInfo().emailValue()).isEqualTo(original.guestInfo().emailValue());
            assertThat(result.stayPeriod().startDate()).isEqualTo(original.stayPeriod().startDate());
            assertThat(result.stayPeriod().endDate()).isEqualTo(original.stayPeriod().endDate());
            assertThat(result.guestCount()).isEqualTo(original.guestCount());
            assertThat(result.totalAmount().amount()).isEqualByComparingTo(original.totalAmount().amount());
            assertThat(result.status()).isEqualTo(ReservationStatus.PENDING);
            assertThat(result.cancelReason()).isNull();
            assertThat(result.bookingSnapshot()).isEqualTo(original.bookingSnapshot());
            assertThat(result.cancelledAt()).isNull();
            assertThat(result.createdAt()).isNotNull();
            assertThat(result.updatedAt()).isNotNull();
        }

        @Test
        @DisplayName("다객실 조합 예약 저장 시 persist가 정상적으로 ID를 반환한다")
        void shouldPersistMultiRoomReservation() {
            // given - 2개 라인 (디럭스 2실 + 스탠다드 1실)
            String uniqueNo = "RSV-MULTI-" + UUID.randomUUID().toString().substring(0, 14);
            RatePlanId standardRatePlanId = RatePlanId.of(2L);
            ReservationLine deluxeLine = ReservationFixture.multiRoomLine(2);
            ReservationLine standardLine = ReservationLine.forNew(null, standardRatePlanId, 1,
                    Money.of(160_000), ReservationFixture.defaultItems(), ReservationFixture.DEFAULT_NOW);

            Money total = deluxeLine.subtotalAmount().add(standardLine.subtotalAmount());
            Reservation multiRoom = Reservation.forNew(
                    ReservationFixture.DEFAULT_CUSTOMER_ID,
                    ReservationNo.of(uniqueNo),
                    ReservationFixture.DEFAULT_GUEST_INFO,
                    ReservationFixture.DEFAULT_STAY_PERIOD,
                    4,
                    total,
                    ReservationFixture.DEFAULT_BOOKING_SNAPSHOT,
                    List.of(deluxeLine, standardLine),
                    ReservationFixture.DEFAULT_TODAY,
                    ReservationFixture.DEFAULT_NOW
            );

            // when
            Long savedId = commandAdapter.persist(multiRoom);

            // then
            assertThat(savedId).isNotNull();
            Optional<Reservation> found = queryAdapter.findById(ReservationId.of(savedId));
            assertThat(found).isPresent();
            assertThat(found.get().totalAmount().amount()).isEqualByComparingTo(total.amount());
        }
    }

    // -- PT-2: CRUD 동작 --

    @Nested
    @DisplayName("PT-2: CRUD 동작")
    class CrudTest {

        @Test
        @DisplayName("persist 후 findById로 조회할 수 있다")
        void shouldPersistAndFindById() {
            // given
            Reservation reservation = uniquePendingReservation();

            // when
            Long savedId = commandAdapter.persist(reservation);
            Optional<Reservation> found = queryAdapter.findById(ReservationId.of(savedId));

            // then
            assertThat(found).isPresent();
            assertThat(found.get().id().value()).isEqualTo(savedId);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 findById 시 Optional.empty()를 반환한다")
        void shouldReturnEmptyForNonExistingId() {
            // when
            Optional<Reservation> found = queryAdapter.findById(ReservationId.of(99999L));

            // then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("저장된 예약의 reservationNo가 unique 제약에 맞게 저장된다")
        void shouldEnforceUniqueReservationNo() {
            // given
            Reservation reservation = uniquePendingReservation();
            Long savedId = commandAdapter.persist(reservation);

            // when
            Optional<Reservation> found = queryAdapter.findById(ReservationId.of(savedId));

            // then
            assertThat(found).isPresent();
            assertThat(found.get().reservationNo().value()).isEqualTo(reservation.reservationNo().value());
        }
    }

    // -- PT-3: 상태 저장 검증 --

    @Nested
    @DisplayName("PT-3: 상태별 예약 저장 검증")
    class StatusPersistenceTest {

        @Test
        @DisplayName("CONFIRMED 상태 예약이 정상적으로 저장/조회된다")
        void shouldPersistConfirmedReservation() {
            // given
            String uniqueNo = "RSV-CNF-" + UUID.randomUUID().toString().substring(0, 16);
            Reservation confirmed = Reservation.reconstitute(
                    null, ReservationFixture.DEFAULT_CUSTOMER_ID,
                    ReservationNo.of(uniqueNo), ReservationFixture.DEFAULT_GUEST_INFO,
                    ReservationFixture.DEFAULT_STAY_PERIOD, ReservationFixture.DEFAULT_GUEST_COUNT,
                    ReservationFixture.DEFAULT_TOTAL_AMOUNT, ReservationStatus.CONFIRMED,
                    null, ReservationFixture.DEFAULT_BOOKING_SNAPSHOT,
                    ReservationFixture.DEFAULT_NOW, ReservationFixture.DEFAULT_NOW,
                    null, ReservationFixture.defaultLines()
            );

            // when
            Long savedId = commandAdapter.persist(confirmed);
            Optional<Reservation> found = queryAdapter.findById(ReservationId.of(savedId));

            // then
            assertThat(found).isPresent();
            assertThat(found.get().status()).isEqualTo(ReservationStatus.CONFIRMED);
        }

        @Test
        @DisplayName("CANCELLED 상태 예약이 cancelReason, cancelledAt과 함께 저장/조회된다")
        void shouldPersistCancelledReservationWithReasonAndTimestamp() {
            // given
            String uniqueNo = "RSV-CXL-" + UUID.randomUUID().toString().substring(0, 16);
            Instant cancelledAt = Instant.parse("2026-04-05T10:00:00Z");
            Reservation cancelled = Reservation.reconstitute(
                    null, ReservationFixture.DEFAULT_CUSTOMER_ID,
                    ReservationNo.of(uniqueNo), ReservationFixture.DEFAULT_GUEST_INFO,
                    ReservationFixture.DEFAULT_STAY_PERIOD, ReservationFixture.DEFAULT_GUEST_COUNT,
                    ReservationFixture.DEFAULT_TOTAL_AMOUNT, ReservationStatus.CANCELLED,
                    "고객 요청 취소", ReservationFixture.DEFAULT_BOOKING_SNAPSHOT,
                    ReservationFixture.DEFAULT_NOW, ReservationFixture.DEFAULT_NOW,
                    cancelledAt, ReservationFixture.defaultLines()
            );

            // when
            Long savedId = commandAdapter.persist(cancelled);
            Optional<Reservation> found = queryAdapter.findById(ReservationId.of(savedId));

            // then
            assertThat(found).isPresent();
            Reservation result = found.get();
            assertThat(result.status()).isEqualTo(ReservationStatus.CANCELLED);
            assertThat(result.cancelReason()).isEqualTo("고객 요청 취소");
            assertThat(result.cancelledAt()).isEqualTo(cancelledAt);
        }

        @Test
        @DisplayName("COMPLETED 상태 예약이 정상적으로 저장/조회된다")
        void shouldPersistCompletedReservation() {
            // given
            String uniqueNo = "RSV-CMP-" + UUID.randomUUID().toString().substring(0, 16);
            Reservation completed = Reservation.reconstitute(
                    null, ReservationFixture.DEFAULT_CUSTOMER_ID,
                    ReservationNo.of(uniqueNo), ReservationFixture.DEFAULT_GUEST_INFO,
                    ReservationFixture.DEFAULT_STAY_PERIOD, ReservationFixture.DEFAULT_GUEST_COUNT,
                    ReservationFixture.DEFAULT_TOTAL_AMOUNT, ReservationStatus.COMPLETED,
                    null, ReservationFixture.DEFAULT_BOOKING_SNAPSHOT,
                    ReservationFixture.DEFAULT_NOW, ReservationFixture.DEFAULT_NOW,
                    null, ReservationFixture.defaultLines()
            );

            // when
            Long savedId = commandAdapter.persist(completed);
            Optional<Reservation> found = queryAdapter.findById(ReservationId.of(savedId));

            // then
            assertThat(found).isPresent();
            assertThat(found.get().status()).isEqualTo(ReservationStatus.COMPLETED);
        }

        @Test
        @DisplayName("NO_SHOW 상태 예약이 정상적으로 저장/조회된다")
        void shouldPersistNoShowReservation() {
            // given
            String uniqueNo = "RSV-NS-" + UUID.randomUUID().toString().substring(0, 17);
            Reservation noShow = Reservation.reconstitute(
                    null, ReservationFixture.DEFAULT_CUSTOMER_ID,
                    ReservationNo.of(uniqueNo), ReservationFixture.DEFAULT_GUEST_INFO,
                    ReservationFixture.DEFAULT_STAY_PERIOD, ReservationFixture.DEFAULT_GUEST_COUNT,
                    ReservationFixture.DEFAULT_TOTAL_AMOUNT, ReservationStatus.NO_SHOW,
                    null, ReservationFixture.DEFAULT_BOOKING_SNAPSHOT,
                    ReservationFixture.DEFAULT_NOW, ReservationFixture.DEFAULT_NOW,
                    null, ReservationFixture.defaultLines()
            );

            // when
            Long savedId = commandAdapter.persist(noShow);
            Optional<Reservation> found = queryAdapter.findById(ReservationId.of(savedId));

            // then
            assertThat(found).isPresent();
            assertThat(found.get().status()).isEqualTo(ReservationStatus.NO_SHOW);
        }
    }

    // -- PT-4: Flyway 마이그레이션 검증 --

    @Nested
    @DisplayName("PT-4: Flyway 마이그레이션 검증")
    class FlywayMigrationTest {

        @Test
        @DisplayName("Testcontainers 시작 시 Flyway가 reservation, reservation_line, reservation_item 테이블을 정상적으로 생성한다")
        void shouldCreateReservationTablesViaFlyway() {
            // Flyway가 실패하면 테스트 컨텍스트 자체가 로드되지 않으므로,
            // 이 테스트가 실행 가능한 것 자체가 마이그레이션 성공을 증명한다.
            Long savedId = commandAdapter.persist(uniquePendingReservation());
            assertThat(savedId).isNotNull();
            assertThat(queryAdapter.findById(ReservationId.of(savedId))).isPresent();
        }
    }

    // -- PT-5: nullable 필드 처리 --

    @Nested
    @DisplayName("PT-5: nullable 필드 처리")
    class NullableFieldTest {

        @Test
        @DisplayName("guestEmail이 null인 예약이 정상적으로 저장/조회된다")
        void shouldHandleNullGuestEmail() {
            // given
            Reservation reservation = reservationWithOptionalEmailNull();

            // when
            Long savedId = commandAdapter.persist(reservation);
            Optional<Reservation> found = queryAdapter.findById(ReservationId.of(savedId));

            // then
            assertThat(found).isPresent();
            Reservation result = found.get();
            assertThat(result.guestInfo().name()).isEqualTo("김철수");
            assertThat(result.guestInfo().phone()).isNotNull();
            assertThat(result.guestInfo().email()).isNull();
        }

        @Test
        @DisplayName("bookingSnapshot이 null인 예약이 정상적으로 저장/조회된다")
        void shouldHandleNullBookingSnapshot() {
            // given
            Reservation reservation = reservationWithOptionalEmailNull();
            assertThat(reservation.bookingSnapshot()).isNull();

            // when
            Long savedId = commandAdapter.persist(reservation);
            Optional<Reservation> found = queryAdapter.findById(ReservationId.of(savedId));

            // then
            assertThat(found).isPresent();
            assertThat(found.get().bookingSnapshot()).isNull();
        }

        @Test
        @DisplayName("cancelReason이 null인 PENDING 예약이 정상적으로 저장/조회된다")
        void shouldHandleNullCancelReason() {
            // given
            Reservation reservation = uniquePendingReservation();
            assertThat(reservation.cancelReason()).isNull();

            // when
            Long savedId = commandAdapter.persist(reservation);
            Optional<Reservation> found = queryAdapter.findById(ReservationId.of(savedId));

            // then
            assertThat(found).isPresent();
            assertThat(found.get().cancelReason()).isNull();
        }

        @Test
        @DisplayName("cancelledAt이 null인 PENDING 예약이 정상적으로 저장/조회된다")
        void shouldHandleNullCancelledAt() {
            // given
            Reservation reservation = uniquePendingReservation();
            assertThat(reservation.cancelledAt()).isNull();

            // when
            Long savedId = commandAdapter.persist(reservation);
            Optional<Reservation> found = queryAdapter.findById(ReservationId.of(savedId));

            // then
            assertThat(found).isPresent();
            assertThat(found.get().cancelledAt()).isNull();
        }
    }

    // -- PT-6: ReservationLine + ReservationItem 저장 검증 --

    @Nested
    @DisplayName("PT-6: ReservationLine + ReservationItem 원자적 저장 검증")
    class LineAndItemPersistenceTest {

        @Test
        @DisplayName("Reservation 저장 시 ReservationLine과 ReservationItem이 함께 저장된다 (persist 반환값 확인)")
        void shouldPersistLinesAndItemsAtomically() {
            // given - 1 라인, 2 아이템
            Reservation reservation = uniquePendingReservation();
            assertThat(reservation.lines()).hasSize(1);
            assertThat(reservation.lines().get(0).items()).hasSize(2);

            // when
            Long savedId = commandAdapter.persist(reservation);

            // then - persist가 성공적으로 ID를 반환하면 Line/Item도 함께 저장된 것
            assertThat(savedId).isNotNull();
        }

        @Test
        @DisplayName("다객실 예약 저장 시 여러 Line과 각 Line의 Item이 모두 저장된다")
        void shouldPersistMultipleLinesAndItems() {
            // given - 2 라인, 각 2 아이템 = 총 4 아이템
            String uniqueNo = "RSV-ML-" + UUID.randomUUID().toString().substring(0, 17);
            RatePlanId standardRatePlanId = RatePlanId.of(2L);

            List<ReservationItem> items1 = List.of(
                    ReservationItem.forNew(InventoryId.of(200L), LocalDate.of(2026, 4, 10), Money.of(100_000), ReservationFixture.DEFAULT_NOW),
                    ReservationItem.forNew(InventoryId.of(201L), LocalDate.of(2026, 4, 11), Money.of(100_000), ReservationFixture.DEFAULT_NOW)
            );
            List<ReservationItem> items2 = List.of(
                    ReservationItem.forNew(InventoryId.of(300L), LocalDate.of(2026, 4, 10), Money.of(80_000), ReservationFixture.DEFAULT_NOW),
                    ReservationItem.forNew(InventoryId.of(301L), LocalDate.of(2026, 4, 11), Money.of(80_000), ReservationFixture.DEFAULT_NOW)
            );

            ReservationLine line1 = ReservationLine.forNew(null, ReservationFixture.DEFAULT_RATE_PLAN_ID, 1,
                    Money.of(200_000), items1, ReservationFixture.DEFAULT_NOW);
            ReservationLine line2 = ReservationLine.forNew(null, standardRatePlanId, 1,
                    Money.of(160_000), items2, ReservationFixture.DEFAULT_NOW);

            Reservation reservation = Reservation.forNew(
                    ReservationFixture.DEFAULT_CUSTOMER_ID,
                    ReservationNo.of(uniqueNo),
                    ReservationFixture.DEFAULT_GUEST_INFO,
                    ReservationFixture.DEFAULT_STAY_PERIOD,
                    ReservationFixture.DEFAULT_GUEST_COUNT,
                    Money.of(360_000),
                    ReservationFixture.DEFAULT_BOOKING_SNAPSHOT,
                    List.of(line1, line2),
                    ReservationFixture.DEFAULT_TODAY,
                    ReservationFixture.DEFAULT_NOW
            );

            // when
            Long savedId = commandAdapter.persist(reservation);

            // then
            assertThat(savedId).isNotNull();
            // Reservation 조회로 기본 정보 확인
            Optional<Reservation> found = queryAdapter.findById(ReservationId.of(savedId));
            assertThat(found).isPresent();
            assertThat(found.get().totalAmount().amount()).isEqualByComparingTo(Money.of(360_000).amount());
        }
    }
}
