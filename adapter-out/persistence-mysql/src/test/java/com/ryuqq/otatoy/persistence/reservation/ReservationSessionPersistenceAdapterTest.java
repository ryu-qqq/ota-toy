package com.ryuqq.otatoy.persistence.reservation;

import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.reservation.ReservationSession;
import com.ryuqq.otatoy.domain.reservation.ReservationSessionFixture;
import com.ryuqq.otatoy.domain.reservation.ReservationSessionStatus;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
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
 * ReservationSession Persistence Adapter 통합 테스트.
 * Testcontainers MySQL + Flyway 기반으로 실제 DB에서 CRUD 동작을 검증한다.
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
class ReservationSessionPersistenceAdapterTest extends MySqlTestContainerConfig {

    @Autowired
    private ReservationSessionCommandAdapter commandAdapter;

    @Autowired
    private ReservationSessionQueryAdapter queryAdapter;

    // === 헬퍼 메서드 ===

    /**
     * 고유한 멱등키를 가진 PENDING 세션을 생성한다.
     * 테스트 간 unique 제약 위반을 방지하기 위해 UUID 기반 멱등키를 사용한다.
     */
    private ReservationSession uniquePendingSession() {
        String uniqueKey = "test-key-" + UUID.randomUUID();
        return ReservationSession.forNew(
                uniqueKey,
                ReservationSessionFixture.DEFAULT_PROPERTY_ID,
                ReservationSessionFixture.DEFAULT_ROOM_TYPE_ID,
                ReservationSessionFixture.DEFAULT_RATE_PLAN_ID,
                ReservationSessionFixture.DEFAULT_CHECK_IN,
                ReservationSessionFixture.DEFAULT_CHECK_OUT,
                ReservationSessionFixture.DEFAULT_GUEST_COUNT,
                ReservationSessionFixture.DEFAULT_TOTAL_AMOUNT,
                ReservationSessionFixture.DEFAULT_NOW
        );
    }

    // -- PT-1: Domain <-> Entity 매핑 정합성 --

    @Nested
    @DisplayName("PT-1: Domain <-> Entity 매핑 정합성")
    class DomainEntityMappingTest {

        @Test
        @DisplayName("PENDING 세션 저장 후 조회 시 모든 필드가 원본과 동일하다")
        void shouldMapAllFieldsCorrectlyForPendingSession() {
            // given
            ReservationSession original = uniquePendingSession();

            // when
            Long savedId = commandAdapter.persist(original);
            Optional<ReservationSession> found = queryAdapter.findById(savedId);

            // then
            assertThat(found).isPresent();
            ReservationSession result = found.get();

            assertThat(result.id().value()).isEqualTo(savedId);
            assertThat(result.idempotencyKey()).isEqualTo(original.idempotencyKey());
            assertThat(result.propertyId().value()).isEqualTo(original.propertyId().value());
            assertThat(result.roomTypeId().value()).isEqualTo(original.roomTypeId().value());
            assertThat(result.ratePlanId().value()).isEqualTo(original.ratePlanId().value());
            assertThat(result.checkIn()).isEqualTo(original.checkIn());
            assertThat(result.checkOut()).isEqualTo(original.checkOut());
            assertThat(result.guestCount()).isEqualTo(original.guestCount());
            assertThat(result.totalAmount().amount()).isEqualByComparingTo(original.totalAmount().amount());
            assertThat(result.status()).isEqualTo(ReservationSessionStatus.PENDING);
            assertThat(result.reservationId()).isNull();
            assertThat(result.createdAt()).isNotNull();
            assertThat(result.updatedAt()).isNotNull();
        }

        @Test
        @DisplayName("CONFIRMED 세션 저장 후 조회 시 reservationId가 정확히 매핑된다")
        void shouldMapConfirmedSessionWithReservationId() {
            // given
            ReservationSession session = uniquePendingSession();
            Long savedId = commandAdapter.persist(session);

            // 조회 후 confirm 처리
            ReservationSession persisted = queryAdapter.findById(savedId).orElseThrow();
            Instant confirmNow = Instant.parse("2026-04-06T00:05:00Z");
            persisted.confirm(999L, confirmNow);

            // when - 상태 변경 후 다시 저장
            commandAdapter.persist(persisted);
            Optional<ReservationSession> found = queryAdapter.findById(savedId);

            // then
            assertThat(found).isPresent();
            ReservationSession result = found.get();
            assertThat(result.status()).isEqualTo(ReservationSessionStatus.CONFIRMED);
            assertThat(result.reservationId()).isEqualTo(999L);
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
            ReservationSession session = uniquePendingSession();

            // when
            Long savedId = commandAdapter.persist(session);
            Optional<ReservationSession> found = queryAdapter.findById(savedId);

            // then
            assertThat(found).isPresent();
            assertThat(found.get().id().value()).isEqualTo(savedId);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 findById 시 Optional.empty()를 반환한다")
        void shouldReturnEmptyForNonExistingId() {
            // when
            Optional<ReservationSession> found = queryAdapter.findById(99999L);

            // then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("세션 상태 변경 후 persist 하면 업데이트된 상태가 반영된다")
        void shouldUpdateSessionStatusOnPersist() {
            // given
            ReservationSession session = uniquePendingSession();
            Long savedId = commandAdapter.persist(session);

            ReservationSession persisted = queryAdapter.findById(savedId).orElseThrow();
            persisted.expire(Instant.parse("2026-04-06T00:15:00Z"));

            // when
            commandAdapter.persist(persisted);
            Optional<ReservationSession> found = queryAdapter.findById(savedId);

            // then
            assertThat(found).isPresent();
            assertThat(found.get().status()).isEqualTo(ReservationSessionStatus.EXPIRED);
        }
    }

    // -- PT-3: 멱등키 기반 조회 --

    @Nested
    @DisplayName("PT-3: 멱등키 기반 조회")
    class IdempotencyKeyQueryTest {

        @Test
        @DisplayName("저장된 세션을 멱등키로 조회할 수 있다")
        void shouldFindByIdempotencyKey() {
            // given
            ReservationSession session = uniquePendingSession();
            commandAdapter.persist(session);

            // when
            Optional<ReservationSession> found = queryAdapter.findByIdempotencyKey(session.idempotencyKey());

            // then
            assertThat(found).isPresent();
            assertThat(found.get().idempotencyKey()).isEqualTo(session.idempotencyKey());
        }

        @Test
        @DisplayName("존재하지 않는 멱등키로 조회 시 Optional.empty()를 반환한다")
        void shouldReturnEmptyForNonExistingIdempotencyKey() {
            // when
            Optional<ReservationSession> found = queryAdapter.findByIdempotencyKey("non-existing-key");

            // then
            assertThat(found).isEmpty();
        }
    }

    // -- PT-4: PENDING 만료 세션 조회 (findPendingBefore) --

    @Nested
    @DisplayName("PT-4: PENDING 만료 세션 조회")
    class PendingBeforeQueryTest {

        @Test
        @DisplayName("cutoff 시각을 미래로 설정하면 현재 시각에 생성된 PENDING 세션이 조회된다")
        void shouldFindPendingSessionsBeforeCutoff() {
            // given - PENDING 세션 생성 (JPA Auditing이 현재 시각을 createdAt으로 설정)
            String uniqueKey = "pending-before-" + UUID.randomUUID();
            ReservationSession session = ReservationSession.forNew(
                    uniqueKey,
                    ReservationSessionFixture.DEFAULT_PROPERTY_ID,
                    ReservationSessionFixture.DEFAULT_ROOM_TYPE_ID,
                    ReservationSessionFixture.DEFAULT_RATE_PLAN_ID,
                    ReservationSessionFixture.DEFAULT_CHECK_IN,
                    ReservationSessionFixture.DEFAULT_CHECK_OUT,
                    ReservationSessionFixture.DEFAULT_GUEST_COUNT,
                    ReservationSessionFixture.DEFAULT_TOTAL_AMOUNT,
                    ReservationSessionFixture.DEFAULT_NOW
            );
            commandAdapter.persist(session);

            // when - cutoff을 충분히 미래로 설정하여 방금 생성한 세션이 포함되도록 함
            Instant cutoff = Instant.now().plusSeconds(60);
            List<ReservationSession> result = queryAdapter.findPendingBefore(cutoff);

            // then
            assertThat(result).anyMatch(s -> s.idempotencyKey().equals(uniqueKey));
        }

        @Test
        @DisplayName("CONFIRMED 상태 세션은 findPendingBefore에서 조회되지 않는다")
        void shouldNotFindConfirmedSessionsInPendingBefore() {
            // given
            ReservationSession session = uniquePendingSession();
            Long savedId = commandAdapter.persist(session);

            ReservationSession persisted = queryAdapter.findById(savedId).orElseThrow();
            persisted.confirm(100L, Instant.parse("2026-04-06T00:01:00Z"));
            commandAdapter.persist(persisted);

            // when
            Instant cutoff = Instant.parse("2026-04-07T00:00:00Z");
            List<ReservationSession> result = queryAdapter.findPendingBefore(cutoff);

            // then
            assertThat(result).noneMatch(s -> s.id().value().equals(savedId));
        }
    }

    // -- PT-5: reservationId 기반 조회 --

    @Nested
    @DisplayName("PT-5: reservationId 기반 조회")
    class ReservationIdQueryTest {

        @Test
        @DisplayName("reservationId로 CONFIRMED 세션을 조회할 수 있다")
        void shouldFindByReservationId() {
            // given
            ReservationSession session = uniquePendingSession();
            Long savedId = commandAdapter.persist(session);

            ReservationSession persisted = queryAdapter.findById(savedId).orElseThrow();
            Long reservationId = 777L;
            persisted.confirm(reservationId, Instant.parse("2026-04-06T00:05:00Z"));
            commandAdapter.persist(persisted);

            // when
            Optional<ReservationSession> found = queryAdapter.findByReservationId(reservationId);

            // then
            assertThat(found).isPresent();
            assertThat(found.get().reservationId()).isEqualTo(reservationId);
            assertThat(found.get().status()).isEqualTo(ReservationSessionStatus.CONFIRMED);
        }

        @Test
        @DisplayName("존재하지 않는 reservationId로 조회 시 Optional.empty()를 반환한다")
        void shouldReturnEmptyForNonExistingReservationId() {
            // when
            Optional<ReservationSession> found = queryAdapter.findByReservationId(88888L);

            // then
            assertThat(found).isEmpty();
        }
    }

    // -- PT-6: Flyway 마이그레이션 검증 --

    @Nested
    @DisplayName("PT-6: Flyway 마이그레이션 검증")
    class FlywayMigrationTest {

        @Test
        @DisplayName("Testcontainers 시작 시 Flyway가 reservation_session 테이블을 정상적으로 생성한다")
        void shouldCreateReservationSessionTableViaFlyway() {
            // Flyway가 실패하면 테스트 컨텍스트 자체가 로드되지 않으므로,
            // 이 테스트가 실행 가능한 것 자체가 마이그레이션 성공을 증명한다.
            Long savedId = commandAdapter.persist(uniquePendingSession());
            assertThat(savedId).isNotNull();
            assertThat(queryAdapter.findById(savedId)).isPresent();
        }
    }

    // -- PT-7: nullable 필드 처리 --

    @Nested
    @DisplayName("PT-7: nullable 필드 처리")
    class NullableFieldTest {

        @Test
        @DisplayName("reservationId가 null인 PENDING 세션이 정상적으로 저장/조회된다")
        void shouldHandleNullReservationIdCorrectly() {
            // given
            ReservationSession session = uniquePendingSession();
            assertThat(session.reservationId()).isNull();

            // when
            Long savedId = commandAdapter.persist(session);
            Optional<ReservationSession> found = queryAdapter.findById(savedId);

            // then
            assertThat(found).isPresent();
            assertThat(found.get().reservationId()).isNull();
        }

        @Test
        @DisplayName("reservationId가 있는 CONFIRMED 세션이 정상적으로 저장/조회된다")
        void shouldHandleNonNullReservationIdCorrectly() {
            // given
            ReservationSession session = uniquePendingSession();
            Long savedId = commandAdapter.persist(session);

            ReservationSession persisted = queryAdapter.findById(savedId).orElseThrow();
            persisted.confirm(555L, Instant.parse("2026-04-06T00:05:00Z"));
            commandAdapter.persist(persisted);

            // when
            Optional<ReservationSession> found = queryAdapter.findById(savedId);

            // then
            assertThat(found).isPresent();
            assertThat(found.get().reservationId()).isEqualTo(555L);
        }
    }
}
