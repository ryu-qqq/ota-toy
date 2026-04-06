package com.ryuqq.otatoy.persistence.pricing;

import com.ryuqq.otatoy.domain.pricing.CancellationPolicy;
import com.ryuqq.otatoy.domain.pricing.PaymentPolicy;
import com.ryuqq.otatoy.domain.pricing.PricingFixtures;
import com.ryuqq.otatoy.domain.pricing.RateOverride;
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
import com.ryuqq.otatoy.persistence.pricing.adapter.RateOverrideCommandAdapter;
import com.ryuqq.otatoy.persistence.pricing.adapter.RateRuleCommandAdapter;
import com.ryuqq.otatoy.persistence.pricing.adapter.RatePlanCommandAdapter;
import com.ryuqq.otatoy.persistence.pricing.entity.RateOverrideJpaEntity;
import com.ryuqq.otatoy.persistence.pricing.mapper.RateOverrideEntityMapper;
import com.ryuqq.otatoy.persistence.pricing.mapper.RateRuleEntityMapper;
import com.ryuqq.otatoy.persistence.pricing.mapper.RatePlanEntityMapper;
import com.ryuqq.otatoy.persistence.pricing.repository.RateOverrideJpaRepository;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RateOverride Persistence Adapter 통합 테스트.
 * Testcontainers MySQL + Flyway 기반으로 RateOverride persistAll 및 Domain <-> Entity 매핑을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        JpaConfig.class,
        RateOverrideCommandAdapter.class,
        RateOverrideEntityMapper.class,
        RateRuleCommandAdapter.class,
        RateRuleEntityMapper.class,
        RatePlanCommandAdapter.class,
        RatePlanEntityMapper.class,
        PropertyCommandAdapter.class,
        PropertyEntityMapper.class,
        RoomTypeCommandAdapter.class,
        RoomTypeEntityMapper.class
})
class RateOverridePersistenceAdapterTest extends MySqlTestContainerConfig {

    @Autowired
    private RateOverrideCommandAdapter rateOverrideCommandAdapter;

    @Autowired
    private RateOverrideJpaRepository rateOverrideJpaRepository;

    @Autowired
    private RateOverrideEntityMapper rateOverrideEntityMapper;

    @Autowired
    private RateRuleCommandAdapter rateRuleCommandAdapter;

    @Autowired
    private RatePlanCommandAdapter ratePlanCommandAdapter;

    @Autowired
    private PropertyCommandAdapter propertyCommandAdapter;

    @Autowired
    private RoomTypeCommandAdapter roomTypeCommandAdapter;

    private Long ratePlanId;
    private Long rateRuleId;

    @BeforeEach
    void setUp() {
        // FK 선행 데이터 준비: Property -> RoomType -> RatePlan -> RateRule
        Long propId = propertyCommandAdapter.persist(PropertyFixture.aPropertyWithName("RateOverride 테스트 호텔"));
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

        RateRule rateRule = RateRule.forNew(
                RatePlanId.of(ratePlanId),
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30),
                BigDecimal.valueOf(100_000), BigDecimal.valueOf(100_000),
                BigDecimal.valueOf(120_000), BigDecimal.valueOf(150_000),
                BigDecimal.valueOf(110_000), PricingFixtures.NOW
        );
        rateRuleId = rateRuleCommandAdapter.persist(rateRule);
    }

    // -- PT-1: Domain <-> Entity 매핑 정합성 --

    @Nested
    @DisplayName("PT-1: Domain <-> Entity 매핑 정합성")
    class DomainEntityMappingTest {

        @Test
        @DisplayName("RateOverride 저장 후 조회 시 모든 필드가 원본과 동일하다")
        void shouldMapAllFieldsCorrectlyForRateOverride() {
            // given
            RateOverride original = RateOverride.forNew(
                    RateRuleId.of(rateRuleId),
                    LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30),
                    LocalDate.of(2026, 4, 5),
                    BigDecimal.valueOf(170_000), "공휴일 특가", PricingFixtures.NOW
            );

            // when
            rateOverrideCommandAdapter.persistAll(List.of(original));
            List<RateOverrideJpaEntity> allEntities = rateOverrideJpaRepository.findAll();

            // then
            assertThat(allEntities).isNotEmpty();
            RateOverrideJpaEntity savedEntity = allEntities.stream()
                    .filter(e -> e.getRateRuleId().equals(rateRuleId) && e.getOverrideDate().equals(LocalDate.of(2026, 4, 5)))
                    .findFirst()
                    .orElseThrow();

            RateOverride result = rateOverrideEntityMapper.toDomain(savedEntity);

            assertThat(result.id().value()).isNotNull();
            assertThat(result.rateRuleId().value()).isEqualTo(rateRuleId);
            assertThat(result.overrideDate()).isEqualTo(LocalDate.of(2026, 4, 5));
            assertThat(result.price()).isEqualByComparingTo(BigDecimal.valueOf(170_000));
            assertThat(result.reason()).isEqualTo("공휴일 특가");
        }

        @Test
        @DisplayName("reason이 null인 RateOverride가 정상적으로 저장/조회된다")
        void shouldHandleNullReasonCorrectly() {
            // given
            RateOverride override = RateOverride.forNew(
                    RateRuleId.of(rateRuleId),
                    LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30),
                    LocalDate.of(2026, 4, 10),
                    BigDecimal.valueOf(90_000), null, PricingFixtures.NOW
            );

            // when
            rateOverrideCommandAdapter.persistAll(List.of(override));
            List<RateOverrideJpaEntity> allEntities = rateOverrideJpaRepository.findAll();

            // then
            RateOverrideJpaEntity savedEntity = allEntities.stream()
                    .filter(e -> e.getRateRuleId().equals(rateRuleId) && e.getOverrideDate().equals(LocalDate.of(2026, 4, 10)))
                    .findFirst()
                    .orElseThrow();

            RateOverride result = rateOverrideEntityMapper.toDomain(savedEntity);
            assertThat(result.reason()).isNull();
            assertThat(result.price()).isEqualByComparingTo(BigDecimal.valueOf(90_000));
        }
    }

    // -- PT-2: persistAll 동작 --

    @Nested
    @DisplayName("PT-2: persistAll 동작")
    class PersistAllTest {

        @Test
        @DisplayName("여러 RateOverride를 한 번에 저장할 수 있다")
        void shouldPersistAllOverridesAtOnce() {
            // given
            List<RateOverride> overrides = List.of(
                    RateOverride.forNew(RateRuleId.of(rateRuleId),
                            LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30),
                            LocalDate.of(2026, 4, 5),
                            BigDecimal.valueOf(170_000), "공휴일", PricingFixtures.NOW),
                    RateOverride.forNew(RateRuleId.of(rateRuleId),
                            LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30),
                            LocalDate.of(2026, 4, 15),
                            BigDecimal.valueOf(90_000), "비수기 할인", PricingFixtures.NOW),
                    RateOverride.forNew(RateRuleId.of(rateRuleId),
                            LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30),
                            LocalDate.of(2026, 4, 20),
                            BigDecimal.valueOf(200_000), "성수기 특가", PricingFixtures.NOW)
            );

            // when
            rateOverrideCommandAdapter.persistAll(overrides);

            // then
            List<RateOverrideJpaEntity> saved = rateOverrideJpaRepository.findAll();
            List<RateOverrideJpaEntity> forThisRule = saved.stream()
                    .filter(e -> e.getRateRuleId().equals(rateRuleId))
                    .toList();

            assertThat(forThisRule).hasSize(3);
            assertThat(forThisRule).extracting(RateOverrideJpaEntity::getOverrideDate)
                    .containsExactlyInAnyOrder(
                            LocalDate.of(2026, 4, 5),
                            LocalDate.of(2026, 4, 15),
                            LocalDate.of(2026, 4, 20)
                    );
        }

        @Test
        @DisplayName("빈 리스트로 persistAll 호출 시 에러 없이 동작한다")
        void shouldHandleEmptyListGracefully() {
            // when & then (예외 없이 완료)
            rateOverrideCommandAdapter.persistAll(List.of());
            assertThat(rateOverrideJpaRepository.findAll()).isNotNull();
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
            RateOverride override = RateOverride.forNew(
                    RateRuleId.of(rateRuleId),
                    LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30),
                    LocalDate.of(2026, 4, 12),
                    new BigDecimal("123456.78"), "정밀도 테스트", PricingFixtures.NOW
            );

            // when
            rateOverrideCommandAdapter.persistAll(List.of(override));

            // then
            List<RateOverrideJpaEntity> allEntities = rateOverrideJpaRepository.findAll();
            RateOverrideJpaEntity saved = allEntities.stream()
                    .filter(e -> e.getOverrideDate().equals(LocalDate.of(2026, 4, 12)))
                    .findFirst()
                    .orElseThrow();

            assertThat(saved.getPrice()).isEqualByComparingTo(new BigDecimal("123456.78"));
        }

        @Test
        @DisplayName("0원 가격이 정상적으로 저장/조회된다")
        void shouldPersistZeroPriceCorrectly() {
            // given
            RateOverride override = RateOverride.forNew(
                    RateRuleId.of(rateRuleId),
                    LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30),
                    LocalDate.of(2026, 4, 25),
                    BigDecimal.ZERO, "무료 프로모션", PricingFixtures.NOW
            );

            // when
            rateOverrideCommandAdapter.persistAll(List.of(override));

            // then
            List<RateOverrideJpaEntity> allEntities = rateOverrideJpaRepository.findAll();
            RateOverrideJpaEntity saved = allEntities.stream()
                    .filter(e -> e.getOverrideDate().equals(LocalDate.of(2026, 4, 25)))
                    .findFirst()
                    .orElseThrow();

            assertThat(saved.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // -- PT-4: Flyway 마이그레이션 검증 --

    @Nested
    @DisplayName("PT-4: Flyway 마이그레이션 검증")
    class FlywayMigrationTest {

        @Test
        @DisplayName("Testcontainers 시작 시 Flyway가 rate_override 테이블을 정상적으로 생성한다")
        void shouldCreateRateOverrideTableViaFlyway() {
            // Flyway가 실패하면 테스트 컨텍스트 자체가 로드되지 않으므로,
            // 이 테스트가 실행 가능한 것 자체가 마이그레이션 성공을 증명한다.
            RateOverride override = RateOverride.forNew(
                    RateRuleId.of(rateRuleId),
                    LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30),
                    LocalDate.of(2026, 4, 3),
                    BigDecimal.valueOf(100_000), "Flyway 검증", PricingFixtures.NOW
            );
            rateOverrideCommandAdapter.persistAll(List.of(override));

            List<RateOverrideJpaEntity> saved = rateOverrideJpaRepository.findAll();
            assertThat(saved).isNotEmpty();
        }
    }

    // -- PT-5: rateRuleId 기반 조회 --

    @Nested
    @DisplayName("PT-5: rateRuleId 기반 필터링")
    class RateRuleIdFilterTest {

        @Test
        @DisplayName("동일 rateRuleId에 속하는 RateOverride만 필터링할 수 있다")
        void shouldFilterOverridesByRateRuleId() {
            // given - 두 개의 다른 RateRule에 각각 오버라이드 저장
            Long secondRuleId = rateRuleCommandAdapter.persist(RateRule.forNew(
                    RatePlanId.of(ratePlanId),
                    LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31),
                    BigDecimal.valueOf(80_000), null, null, null, null,
                    PricingFixtures.NOW
            ));

            rateOverrideCommandAdapter.persistAll(List.of(
                    RateOverride.forNew(RateRuleId.of(rateRuleId),
                            LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30),
                            LocalDate.of(2026, 4, 5),
                            BigDecimal.valueOf(170_000), "4월 오버라이드", PricingFixtures.NOW),
                    RateOverride.forNew(RateRuleId.of(secondRuleId),
                            LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31),
                            LocalDate.of(2026, 5, 10),
                            BigDecimal.valueOf(60_000), "5월 오버라이드", PricingFixtures.NOW)
            ));

            // when
            List<RateOverrideJpaEntity> allEntities = rateOverrideJpaRepository.findAll();

            // then - rateRuleId 기준으로 필터링 가능
            List<RateOverrideJpaEntity> firstRuleOverrides = allEntities.stream()
                    .filter(e -> e.getRateRuleId().equals(rateRuleId))
                    .toList();
            List<RateOverrideJpaEntity> secondRuleOverrides = allEntities.stream()
                    .filter(e -> e.getRateRuleId().equals(secondRuleId))
                    .toList();

            assertThat(firstRuleOverrides).hasSize(1);
            assertThat(firstRuleOverrides.getFirst().getOverrideDate()).isEqualTo(LocalDate.of(2026, 4, 5));

            assertThat(secondRuleOverrides).hasSize(1);
            assertThat(secondRuleOverrides.getFirst().getOverrideDate()).isEqualTo(LocalDate.of(2026, 5, 10));
        }
    }
}
