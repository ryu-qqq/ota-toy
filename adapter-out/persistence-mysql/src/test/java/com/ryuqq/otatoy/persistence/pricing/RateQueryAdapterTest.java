package com.ryuqq.otatoy.persistence.pricing;

import com.ryuqq.otatoy.domain.pricing.Rate;
import com.ryuqq.otatoy.domain.pricing.RatePlan;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.domain.pricing.PricingFixtures;
import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertyFixture;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.roomtype.RoomType;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeName;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.persistence.MySqlTestContainerConfig;
import com.ryuqq.otatoy.persistence.config.JpaConfig;
import com.ryuqq.otatoy.persistence.config.QueryDslConfig;
import com.ryuqq.otatoy.persistence.pricing.adapter.RateCommandAdapter;
import com.ryuqq.otatoy.persistence.pricing.adapter.RateQueryAdapter;
import com.ryuqq.otatoy.persistence.pricing.adapter.RatePlanCommandAdapter;
import com.ryuqq.otatoy.persistence.pricing.adapter.RatePlanQueryAdapter;
import com.ryuqq.otatoy.persistence.pricing.mapper.RateEntityMapper;
import com.ryuqq.otatoy.persistence.pricing.mapper.RatePlanEntityMapper;
import com.ryuqq.otatoy.persistence.pricing.repository.RateQueryDslRepository;
import com.ryuqq.otatoy.persistence.pricing.repository.RatePlanQueryDslRepository;
import com.ryuqq.otatoy.persistence.property.adapter.PropertyCommandAdapter;
import com.ryuqq.otatoy.persistence.property.mapper.PropertyEntityMapper;
import com.ryuqq.otatoy.persistence.roomtype.adapter.RoomTypeCommandAdapter;
import com.ryuqq.otatoy.persistence.roomtype.mapper.RoomTypeEntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
 * Rate Query Adapter 통합 테스트.
 * RatePlanId + 날짜 범위 기반 조회를 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        JpaConfig.class,
        QueryDslConfig.class,
        RateCommandAdapter.class,
        RateQueryAdapter.class,
        RatePlanCommandAdapter.class,
        RatePlanQueryAdapter.class,
        RateEntityMapper.class,
        RatePlanEntityMapper.class,
        RateQueryDslRepository.class,
        RatePlanQueryDslRepository.class,
        PropertyCommandAdapter.class,
        PropertyEntityMapper.class,
        RoomTypeCommandAdapter.class,
        RoomTypeEntityMapper.class
})
class RateQueryAdapterTest extends MySqlTestContainerConfig {

    @Autowired
    private RateCommandAdapter rateCommandAdapter;

    @Autowired
    private RateQueryAdapter rateQueryAdapter;

    @Autowired
    private RatePlanCommandAdapter ratePlanCommandAdapter;

    @Autowired
    private PropertyCommandAdapter propertyCommandAdapter;

    @Autowired
    private RoomTypeCommandAdapter roomTypeCommandAdapter;

    private Long ratePlanId;

    @BeforeEach
    void setUp() {
        // 숙소 -> 객실 -> RatePlan -> Rate 순으로 생성
        Long propId = propertyCommandAdapter.persist(PropertyFixture.aPropertyWithName("요금 테스트 호텔"));
        Long roomId = roomTypeCommandAdapter.persist(
                RoomType.forNew(PropertyId.of(propId), RoomTypeName.of("테스트 룸"),
                        null, null, null, 2, 4, 3, null, null, Instant.now()));

        RatePlan ratePlan = com.ryuqq.otatoy.domain.pricing.RatePlan.forNew(
                RoomTypeId.of(roomId), com.ryuqq.otatoy.domain.pricing.RatePlanName.of("기본 요금"),
                com.ryuqq.otatoy.domain.pricing.SourceType.DIRECT, null,
                com.ryuqq.otatoy.domain.pricing.CancellationPolicy.defaultPolicy(),
                com.ryuqq.otatoy.domain.pricing.PaymentPolicy.PREPAY, Instant.now()
        );
        ratePlanId = ratePlanCommandAdapter.persist(ratePlan);

        // 4/10 ~ 4/14 5일치 Rate 생성
        List<Rate> rates = new java.util.ArrayList<>();
        for (int i = 10; i <= 14; i++) {
            rates.add(Rate.forNew(
                    RatePlanId.of(ratePlanId),
                    LocalDate.of(2026, 4, i),
                    BigDecimal.valueOf(100_000 + i * 1000),
                    Instant.now()
            ));
        }
        rateCommandAdapter.persistAll(rates);
    }

    @Test
    @DisplayName("날짜 범위 내 Rate만 조회된다 (startDate 포함, endDate 미포함)")
    void shouldFindRatesByDateRange() {
        // when - 4/11 ~ 4/13 (3일치)
        List<Rate> rates = rateQueryAdapter.findByRatePlanIdsAndDateRange(
                List.of(RatePlanId.of(ratePlanId)),
                LocalDate.of(2026, 4, 11),
                LocalDate.of(2026, 4, 14)
        );

        // then
        assertThat(rates).hasSize(3);
        assertThat(rates).allMatch(r -> !r.rateDate().isBefore(LocalDate.of(2026, 4, 11)));
        assertThat(rates).allMatch(r -> r.rateDate().isBefore(LocalDate.of(2026, 4, 14)));
    }

    @Test
    @DisplayName("존재하지 않는 RatePlanId로 조회 시 빈 리스트를 반환한다")
    void shouldReturnEmptyForNonExistingRatePlanId() {
        List<Rate> rates = rateQueryAdapter.findByRatePlanIdsAndDateRange(
                List.of(RatePlanId.of(99999L)),
                LocalDate.of(2026, 4, 10),
                LocalDate.of(2026, 4, 15)
        );
        assertThat(rates).isEmpty();
    }

    @Test
    @DisplayName("Rate의 모든 필드가 정합성을 유지한다")
    void shouldMapAllFieldsCorrectly() {
        List<Rate> rates = rateQueryAdapter.findByRatePlanIdsAndDateRange(
                List.of(RatePlanId.of(ratePlanId)),
                LocalDate.of(2026, 4, 10),
                LocalDate.of(2026, 4, 11)
        );

        assertThat(rates).hasSize(1);
        Rate result = rates.getFirst();
        assertThat(result.id()).isNotNull();
        assertThat(result.ratePlanId().value()).isEqualTo(ratePlanId);
        assertThat(result.rateDate()).isEqualTo(LocalDate.of(2026, 4, 10));
        assertThat(result.basePrice()).isEqualByComparingTo(BigDecimal.valueOf(110_000));
    }
}
