package com.ryuqq.otatoy.persistence.pricing;

import com.ryuqq.otatoy.domain.pricing.CancellationPolicy;
import com.ryuqq.otatoy.domain.pricing.PaymentPolicy;
import com.ryuqq.otatoy.domain.pricing.PricingFixtures;
import com.ryuqq.otatoy.domain.pricing.RatePlan;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.domain.pricing.RatePlanName;
import com.ryuqq.otatoy.domain.pricing.SourceType;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.persistence.MySqlTestContainerConfig;
import com.ryuqq.otatoy.persistence.config.JpaConfig;
import com.ryuqq.otatoy.persistence.config.QueryDslConfig;
import com.ryuqq.otatoy.persistence.pricing.adapter.RatePlanCommandAdapter;
import com.ryuqq.otatoy.persistence.pricing.adapter.RatePlanQueryAdapter;
import com.ryuqq.otatoy.persistence.pricing.mapper.RatePlanEntityMapper;
import com.ryuqq.otatoy.persistence.pricing.repository.RatePlanQueryDslRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RatePlan Persistence Adapter 통합 테스트.
 * Testcontainers MySQL + Flyway 기반으로 실제 DB에서 CRUD 동작을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        JpaConfig.class,
        QueryDslConfig.class,
        RatePlanCommandAdapter.class,
        RatePlanQueryAdapter.class,
        RatePlanEntityMapper.class,
        RatePlanQueryDslRepository.class
})
class RatePlanPersistenceAdapterTest extends MySqlTestContainerConfig {

    @Autowired
    private RatePlanCommandAdapter ratePlanCommandAdapter;

    @Autowired
    private RatePlanQueryAdapter ratePlanQueryAdapter;

    // -- PT-1: Domain <-> Entity 매핑 정합성 --

    @Nested
    @DisplayName("PT-1: Domain <-> Entity 매핑 정합성")
    class DomainEntityMappingTest {

        @Test
        @DisplayName("DIRECT RatePlan 저장 후 조회 시 모든 필드가 원본과 동일하다 (CancellationPolicy, PaymentPolicy 포함)")
        void shouldMapAllFieldsCorrectlyForDirectRatePlan() {
            // given
            RatePlan original = PricingFixtures.directRatePlan();

            // when
            Long savedId = ratePlanCommandAdapter.persist(original);
            Optional<RatePlan> found = ratePlanQueryAdapter.findById(RatePlanId.of(savedId));

            // then
            assertThat(found).isPresent();
            RatePlan result = found.get();

            assertThat(result.id().value()).isEqualTo(savedId);
            assertThat(result.roomTypeId().value()).isEqualTo(PricingFixtures.ROOM_TYPE_ID.value());
            assertThat(result.name().value()).isEqualTo(PricingFixtures.DEFAULT_NAME.value());
            assertThat(result.sourceType()).isEqualTo(SourceType.DIRECT);
            assertThat(result.supplierId()).isNull();
            assertThat(result.paymentPolicy()).isEqualTo(PaymentPolicy.PREPAY);

            // CancellationPolicy 필드별 검증
            CancellationPolicy cp = result.cancellationPolicy();
            assertThat(cp).isNotNull();
            assertThat(cp.freeCancellation()).isTrue();
            assertThat(cp.nonRefundable()).isFalse();
            assertThat(cp.deadlineDays()).isEqualTo(3);
            assertThat(cp.policyText()).isEqualTo("체크인 3일 전까지 무료 취소");
        }

        @Test
        @DisplayName("SUPPLIER RatePlan 저장 후 조회 시 supplierId와 환불불가 정책이 정확히 매핑된다")
        void shouldMapAllFieldsCorrectlyForSupplierRatePlan() {
            // given
            RatePlan original = PricingFixtures.supplierRatePlan();

            // when
            Long savedId = ratePlanCommandAdapter.persist(original);
            Optional<RatePlan> found = ratePlanQueryAdapter.findById(RatePlanId.of(savedId));

            // then
            assertThat(found).isPresent();
            RatePlan result = found.get();

            assertThat(result.sourceType()).isEqualTo(SourceType.SUPPLIER);
            assertThat(result.supplierId()).isNotNull();
            assertThat(result.supplierId().value()).isEqualTo(PricingFixtures.SUPPLIER_ID.value());

            // 환불 불가 정책 검증
            CancellationPolicy cp = result.cancellationPolicy();
            assertThat(cp.freeCancellation()).isFalse();
            assertThat(cp.nonRefundable()).isTrue();
            assertThat(cp.deadlineDays()).isEqualTo(0);
            assertThat(cp.policyText()).isEqualTo("환불 불가");
        }
    }

    // -- PT-2: CRUD 동작 --

    @Nested
    @DisplayName("PT-2: Repository CRUD 동작")
    class CrudTest {

        @Test
        @DisplayName("persist 후 findById로 조회할 수 있다")
        void shouldPersistAndFindById() {
            // given
            RatePlan ratePlan = PricingFixtures.directRatePlan();

            // when
            Long savedId = ratePlanCommandAdapter.persist(ratePlan);
            Optional<RatePlan> found = ratePlanQueryAdapter.findById(RatePlanId.of(savedId));

            // then
            assertThat(found).isPresent();
            assertThat(found.get().id().value()).isEqualTo(savedId);
            assertThat(found.get().name().value()).isEqualTo(PricingFixtures.DEFAULT_NAME.value());
        }

        @Test
        @DisplayName("존재하지 않는 ID로 findById 시 Optional.empty()를 반환한다")
        void shouldReturnEmptyForNonExistingId() {
            // when
            Optional<RatePlan> found = ratePlanQueryAdapter.findById(RatePlanId.of(99999L));

            // then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("findByRoomTypeIds로 특정 객실유형의 RatePlan 목록을 조회할 수 있다")
        void shouldFindByRoomTypeIds() {
            // given
            RoomTypeId roomTypeId1 = RoomTypeId.of(100L);
            RoomTypeId roomTypeId2 = RoomTypeId.of(200L);

            RatePlan plan1 = RatePlan.forNew(roomTypeId1, RatePlanName.of("요금제A"),
                    SourceType.DIRECT, null, PricingFixtures.FREE_CANCELLATION,
                    PaymentPolicy.PREPAY, PricingFixtures.NOW);
            RatePlan plan2 = RatePlan.forNew(roomTypeId2, RatePlanName.of("요금제B"),
                    SourceType.DIRECT, null, PricingFixtures.NON_REFUNDABLE,
                    PaymentPolicy.PAY_AT_PROPERTY, PricingFixtures.NOW);

            ratePlanCommandAdapter.persist(plan1);
            ratePlanCommandAdapter.persist(plan2);

            // when
            List<RatePlan> results = ratePlanQueryAdapter.findByRoomTypeIds(List.of(roomTypeId1, roomTypeId2));

            // then
            assertThat(results).hasSizeGreaterThanOrEqualTo(2);
            assertThat(results).anyMatch(r -> r.roomTypeId().value().equals(100L));
            assertThat(results).anyMatch(r -> r.roomTypeId().value().equals(200L));
        }
    }

    // -- PT-3: existsById 동작 --

    @Nested
    @DisplayName("PT-3: existsById 동작")
    class ExistsByIdTest {

        @Test
        @DisplayName("저장된 RatePlan에 대해 existsById는 true를 반환한다")
        void shouldReturnTrueForExistingRatePlan() {
            // given
            Long savedId = ratePlanCommandAdapter.persist(PricingFixtures.directRatePlan());

            // when & then
            assertThat(ratePlanQueryAdapter.existsById(RatePlanId.of(savedId))).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 ID에 대해 existsById는 false를 반환한다")
        void shouldReturnFalseForNonExistingRatePlan() {
            // when & then
            assertThat(ratePlanQueryAdapter.existsById(RatePlanId.of(99999L))).isFalse();
        }
    }

    // -- PT-4: Flyway 마이그레이션 검증 --

    @Nested
    @DisplayName("PT-4: Flyway 마이그레이션 검증")
    class FlywayMigrationTest {

        @Test
        @DisplayName("Testcontainers 시작 시 Flyway가 rate_plan 테이블을 정상적으로 생성한다")
        void shouldCreateRatePlanTableViaFlyway() {
            // Flyway가 실패하면 테스트 컨텍스트 자체가 로드되지 않으므로,
            // 이 테스트가 실행 가능한 것 자체가 마이그레이션 성공을 증명한다.
            // 추가로 실제 CRUD 동작이 성공하는지 확인한다.
            Long savedId = ratePlanCommandAdapter.persist(PricingFixtures.directRatePlan());
            assertThat(savedId).isNotNull();
            assertThat(ratePlanQueryAdapter.findById(RatePlanId.of(savedId))).isPresent();
        }
    }

    // -- PT-5: nullable 필드 (supplierId null) 처리 --

    @Nested
    @DisplayName("PT-5: nullable 필드 처리")
    class NullableFieldTest {

        @Test
        @DisplayName("supplierId가 null인 DIRECT RatePlan이 정상적으로 저장/조회된다")
        void shouldHandleNullSupplierIdCorrectly() {
            // given
            RatePlan directPlan = PricingFixtures.directRatePlan();
            assertThat(directPlan.supplierId()).isNull(); // 전제 조건 확인

            // when
            Long savedId = ratePlanCommandAdapter.persist(directPlan);
            Optional<RatePlan> found = ratePlanQueryAdapter.findById(RatePlanId.of(savedId));

            // then
            assertThat(found).isPresent();
            assertThat(found.get().supplierId()).isNull();
        }

        @Test
        @DisplayName("supplierId가 있는 SUPPLIER RatePlan이 정상적으로 저장/조회된다")
        void shouldHandleNonNullSupplierIdCorrectly() {
            // given
            RatePlan supplierPlan = PricingFixtures.supplierRatePlan();
            assertThat(supplierPlan.supplierId()).isNotNull(); // 전제 조건 확인

            // when
            Long savedId = ratePlanCommandAdapter.persist(supplierPlan);
            Optional<RatePlan> found = ratePlanQueryAdapter.findById(RatePlanId.of(savedId));

            // then
            assertThat(found).isPresent();
            assertThat(found.get().supplierId()).isNotNull();
            assertThat(found.get().supplierId().value()).isEqualTo(PricingFixtures.SUPPLIER_ID.value());
        }

        @Test
        @DisplayName("cancellationPolicyText가 null이어도 정상 저장/조회된다")
        void shouldHandleNullCancellationPolicyTextCorrectly() {
            // given
            CancellationPolicy policyWithoutText = CancellationPolicy.of(false, false, 0, null);
            RatePlan plan = RatePlan.forNew(
                    PricingFixtures.ROOM_TYPE_ID, RatePlanName.of("텍스트 없는 정책"),
                    SourceType.DIRECT, null, policyWithoutText,
                    PaymentPolicy.PREPAY, PricingFixtures.NOW
            );

            // when
            Long savedId = ratePlanCommandAdapter.persist(plan);
            Optional<RatePlan> found = ratePlanQueryAdapter.findById(RatePlanId.of(savedId));

            // then
            assertThat(found).isPresent();
            CancellationPolicy cp = found.get().cancellationPolicy();
            assertThat(cp.policyText()).isNull();
            assertThat(cp.freeCancellation()).isFalse();
            assertThat(cp.nonRefundable()).isFalse();
            assertThat(cp.deadlineDays()).isEqualTo(0);
        }
    }
}
