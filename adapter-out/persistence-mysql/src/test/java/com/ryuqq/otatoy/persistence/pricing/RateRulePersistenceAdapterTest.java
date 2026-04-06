package com.ryuqq.otatoy.persistence.pricing;

import com.ryuqq.otatoy.domain.pricing.CancellationPolicy;
import com.ryuqq.otatoy.domain.pricing.PaymentPolicy;
import com.ryuqq.otatoy.domain.pricing.PricingFixtures;
import com.ryuqq.otatoy.domain.pricing.RatePlan;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.domain.pricing.RatePlanName;
import com.ryuqq.otatoy.domain.pricing.RateRule;
import com.ryuqq.otatoy.domain.pricing.RateRuleId;
import com.ryuqq.otatoy.domain.pricing.SourceType;
import com.ryuqq.otatoy.domain.property.PropertyFixture;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.roomtype.RoomType;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeName;
import com.ryuqq.otatoy.persistence.MySqlTestContainerConfig;
import com.ryuqq.otatoy.persistence.config.JpaConfig;
import com.ryuqq.otatoy.persistence.config.QueryDslConfig;
import com.ryuqq.otatoy.persistence.pricing.adapter.RateRuleCommandAdapter;
import com.ryuqq.otatoy.persistence.pricing.adapter.RatePlanCommandAdapter;
import com.ryuqq.otatoy.persistence.pricing.mapper.RateRuleEntityMapper;
import com.ryuqq.otatoy.persistence.pricing.mapper.RatePlanEntityMapper;
import com.ryuqq.otatoy.persistence.pricing.entity.RateRuleJpaEntity;
import com.ryuqq.otatoy.persistence.pricing.repository.RateRuleJpaRepository;
import com.ryuqq.otatoy.persistence.property.adapter.PropertyCommandAdapter;
import com.ryuqq.otatoy.persistence.property.mapper.PropertyEntityMapper;
import com.ryuqq.otatoy.persistence.roomtype.adapter.RoomTypeCommandAdapter;
import com.ryuqq.otatoy.persistence.roomtype.mapper.RoomTypeEntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RateRule Persistence Adapter 통합 테스트.
 * Testcontainers MySQL + Flyway 기반으로 RateRule CRUD 및 Domain <-> Entity 매핑을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        JpaConfig.class,
        RateRuleCommandAdapter.class,
        RateRuleEntityMapper.class,
        RatePlanCommandAdapter.class,
        RatePlanEntityMapper.class,
        PropertyCommandAdapter.class,
        PropertyEntityMapper.class,
        RoomTypeCommandAdapter.class,
        RoomTypeEntityMapper.class
})
class RateRulePersistenceAdapterTest extends MySqlTestContainerConfig {

    @Autowired
    private RateRuleCommandAdapter rateRuleCommandAdapter;

    @Autowired
    private RateRuleJpaRepository rateRuleJpaRepository;

    @Autowired
    private RateRuleEntityMapper rateRuleEntityMapper;

    @Autowired
    private RatePlanCommandAdapter ratePlanCommandAdapter;

    @Autowired
    private PropertyCommandAdapter propertyCommandAdapter;

    @Autowired
    private RoomTypeCommandAdapter roomTypeCommandAdapter;

    private Long ratePlanId;

    @BeforeEach
    void setUp() {
        // FK 선행 데이터 준비: Property -> RoomType -> RatePlan
        Long propId = propertyCommandAdapter.persist(PropertyFixture.aPropertyWithName("RateRule 테스트 호텔"));
        Long roomId = roomTypeCommandAdapter.persist(
                RoomType.forNew(PropertyId.of(propId), RoomTypeName.of("테스트 룸"),
                        null, null, null, 2, 4, 3, null, null, Instant.now()));

        RatePlan ratePlan = RatePlan.forNew(
                RoomTypeId.of(roomId), RatePlanName.of("기본 요금제"),
                SourceType.DIRECT, null,
                CancellationPolicy.defaultPolicy(),
                PaymentPolicy.PREPAY, Instant.now()
        );
        ratePlanId = ratePlanCommandAdapter.persist(ratePlan);
    }

    // -- PT-1: Domain <-> Entity 매핑 정합성 --

    @Nested
    @DisplayName("PT-1: Domain <-> Entity 매핑 정합성")
    class DomainEntityMappingTest {

        @Test
        @DisplayName("모든 요일별 가격이 있는 RateRule 저장 후 조회 시 모든 필드가 원본과 동일하다")
        void shouldMapAllFieldsCorrectlyForFullPriceRateRule() {
            // given
            RateRule original = RateRule.forNew(
                    RatePlanId.of(ratePlanId),
                    LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30),
                    BigDecimal.valueOf(100_000), BigDecimal.valueOf(100_000),
                    BigDecimal.valueOf(120_000), BigDecimal.valueOf(150_000),
                    BigDecimal.valueOf(110_000), PricingFixtures.NOW
            );

            // when
            Long savedId = rateRuleCommandAdapter.persist(original);
            Optional<RateRuleJpaEntity> found = rateRuleJpaRepository.findById(savedId);

            // then
            assertThat(found).isPresent();
            RateRule result = rateRuleEntityMapper.toDomain(found.get());

            assertThat(result.id().value()).isEqualTo(savedId);
            assertThat(result.ratePlanId().value()).isEqualTo(ratePlanId);
            assertThat(result.startDate()).isEqualTo(LocalDate.of(2026, 4, 1));
            assertThat(result.endDate()).isEqualTo(LocalDate.of(2026, 4, 30));
            assertThat(result.basePrice()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
            assertThat(result.weekdayPrice()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
            assertThat(result.fridayPrice()).isEqualByComparingTo(BigDecimal.valueOf(120_000));
            assertThat(result.saturdayPrice()).isEqualByComparingTo(BigDecimal.valueOf(150_000));
            assertThat(result.sundayPrice()).isEqualByComparingTo(BigDecimal.valueOf(110_000));
        }

        @Test
        @DisplayName("요일별 가격이 null인 RateRule 저장 후 조회 시 null 필드가 유지된다 (basePrice만 존재)")
        void shouldMapNullDayPricesCorrectly() {
            // given
            RateRule original = RateRule.forNew(
                    RatePlanId.of(ratePlanId),
                    LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30),
                    BigDecimal.valueOf(80_000), null, null, null, null,
                    PricingFixtures.NOW
            );

            // when
            Long savedId = rateRuleCommandAdapter.persist(original);
            Optional<RateRuleJpaEntity> found = rateRuleJpaRepository.findById(savedId);

            // then
            assertThat(found).isPresent();
            RateRule result = rateRuleEntityMapper.toDomain(found.get());

            assertThat(result.basePrice()).isEqualByComparingTo(BigDecimal.valueOf(80_000));
            assertThat(result.weekdayPrice()).isNull();
            assertThat(result.fridayPrice()).isNull();
            assertThat(result.saturdayPrice()).isNull();
            assertThat(result.sundayPrice()).isNull();
        }
    }

    // -- PT-2: CRUD 동작 --

    @Nested
    @DisplayName("PT-2: CRUD 동작")
    class CrudTest {

        @Test
        @DisplayName("persist 후 ID가 반환되고 JpaRepository로 조회할 수 있다")
        void shouldPersistAndReturnId() {
            // given
            RateRule rateRule = RateRule.forNew(
                    RatePlanId.of(ratePlanId),
                    LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31),
                    BigDecimal.valueOf(90_000), null, null, null, null,
                    PricingFixtures.NOW
            );

            // when
            Long savedId = rateRuleCommandAdapter.persist(rateRule);

            // then
            assertThat(savedId).isNotNull();
            assertThat(rateRuleJpaRepository.findById(savedId)).isPresent();
        }

        @Test
        @DisplayName("여러 RateRule을 동일 RatePlan에 저장할 수 있다")
        void shouldPersistMultipleRateRulesForSameRatePlan() {
            // given
            RateRule rule1 = RateRule.forNew(
                    RatePlanId.of(ratePlanId),
                    LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 15),
                    BigDecimal.valueOf(100_000), null, null, null, null,
                    PricingFixtures.NOW
            );
            RateRule rule2 = RateRule.forNew(
                    RatePlanId.of(ratePlanId),
                    LocalDate.of(2026, 4, 16), LocalDate.of(2026, 4, 30),
                    BigDecimal.valueOf(110_000), null, null, null, null,
                    PricingFixtures.NOW
            );

            // when
            Long id1 = rateRuleCommandAdapter.persist(rule1);
            Long id2 = rateRuleCommandAdapter.persist(rule2);

            // then
            assertThat(id1).isNotEqualTo(id2);
            assertThat(rateRuleJpaRepository.findById(id1)).isPresent();
            assertThat(rateRuleJpaRepository.findById(id2)).isPresent();
        }
    }

    // -- PT-3: BigDecimal 정밀도 검증 --

    @Nested
    @DisplayName("PT-3: BigDecimal 정밀도 검증")
    class BigDecimalPrecisionTest {

        @Test
        @DisplayName("소수점 2자리 가격이 정확하게 저장/조회된다")
        void shouldPersistDecimalPrecisionCorrectly() {
            // given
            RateRule rateRule = RateRule.forNew(
                    RatePlanId.of(ratePlanId),
                    LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30),
                    new BigDecimal("99999.99"),
                    new BigDecimal("85000.50"),
                    new BigDecimal("120000.75"),
                    new BigDecimal("150000.00"),
                    new BigDecimal("110000.25"),
                    PricingFixtures.NOW
            );

            // when
            Long savedId = rateRuleCommandAdapter.persist(rateRule);
            Optional<RateRuleJpaEntity> found = rateRuleJpaRepository.findById(savedId);

            // then
            assertThat(found).isPresent();
            RateRule result = rateRuleEntityMapper.toDomain(found.get());

            assertThat(result.basePrice()).isEqualByComparingTo(new BigDecimal("99999.99"));
            assertThat(result.weekdayPrice()).isEqualByComparingTo(new BigDecimal("85000.50"));
            assertThat(result.fridayPrice()).isEqualByComparingTo(new BigDecimal("120000.75"));
            assertThat(result.saturdayPrice()).isEqualByComparingTo(new BigDecimal("150000.00"));
            assertThat(result.sundayPrice()).isEqualByComparingTo(new BigDecimal("110000.25"));
        }

        @Test
        @DisplayName("0원 가격이 정상적으로 저장/조회된다")
        void shouldPersistZeroPriceCorrectly() {
            // given
            RateRule rateRule = RateRule.forNew(
                    RatePlanId.of(ratePlanId),
                    LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31),
                    BigDecimal.ZERO, null, null, null, null,
                    PricingFixtures.NOW
            );

            // when
            Long savedId = rateRuleCommandAdapter.persist(rateRule);
            Optional<RateRuleJpaEntity> found = rateRuleJpaRepository.findById(savedId);

            // then
            assertThat(found).isPresent();
            RateRule result = rateRuleEntityMapper.toDomain(found.get());
            assertThat(result.basePrice()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // -- PT-4: Flyway 마이그레이션 검증 --

    @Nested
    @DisplayName("PT-4: Flyway 마이그레이션 검증")
    class FlywayMigrationTest {

        @Test
        @DisplayName("Testcontainers 시작 시 Flyway가 rate_rule 테이블을 정상적으로 생성한다")
        void shouldCreateRateRuleTableViaFlyway() {
            // Flyway가 실패하면 테스트 컨텍스트 자체가 로드되지 않으므로,
            // 이 테스트가 실행 가능한 것 자체가 마이그레이션 성공을 증명한다.
            Long savedId = rateRuleCommandAdapter.persist(RateRule.forNew(
                    RatePlanId.of(ratePlanId),
                    LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31),
                    BigDecimal.valueOf(100_000), null, null, null, null,
                    PricingFixtures.NOW
            ));
            assertThat(savedId).isNotNull();
            assertThat(rateRuleJpaRepository.findById(savedId)).isPresent();
        }
    }
}
