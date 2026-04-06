package com.ryuqq.otatoy.application.pricing.factory;

import com.ryuqq.otatoy.application.pricing.dto.query.CustomerGetRateQuery;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.property.RateFetchCriteria;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RateCriteriaFactory 단위 테스트.
 * Query DTO -> Domain Criteria 변환 로직을 검증한다.
 * 외부 의존이 없으므로 Mock 없이 테스트한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
class RateCriteriaFactoryTest {

    RateCriteriaFactory factory = new RateCriteriaFactory();

    private static final PropertyId PROPERTY_ID = PropertyId.of(100L);
    private static final LocalDate CHECK_IN = LocalDate.of(2026, 4, 10);
    private static final LocalDate CHECK_OUT = LocalDate.of(2026, 4, 12);
    private static final int GUESTS = 2;

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Query의 모든 필드가 Criteria에 정확히 매핑된다")
        void shouldMapAllFieldsCorrectly() {
            // given
            var query = new CustomerGetRateQuery(PROPERTY_ID, CHECK_IN, CHECK_OUT, GUESTS);

            // when
            RateFetchCriteria criteria = factory.create(query);

            // then
            assertThat(criteria.propertyId()).isEqualTo(PROPERTY_ID);
            assertThat(criteria.checkIn()).isEqualTo(CHECK_IN);
            assertThat(criteria.checkOut()).isEqualTo(CHECK_OUT);
            assertThat(criteria.guests()).isEqualTo(GUESTS);
        }

        @Test
        @DisplayName("1박 숙박 Query를 변환하면 stayDates가 1개이다")
        void shouldCreateCriteriaWithSingleNight() {
            // given
            var query = new CustomerGetRateQuery(PROPERTY_ID,
                LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 11), 1);

            // when
            RateFetchCriteria criteria = factory.create(query);

            // then
            assertThat(criteria.stayDates()).hasSize(1);
            assertThat(criteria.stayDates().get(0)).isEqualTo(LocalDate.of(2026, 4, 10));
        }

        @Test
        @DisplayName("여러 박 숙박 Query를 변환하면 체크아웃 전날까지의 날짜가 포함된다")
        void shouldCreateCriteriaWithMultipleNights() {
            // given -- 4/10 ~ 4/12, 2박
            var query = new CustomerGetRateQuery(PROPERTY_ID, CHECK_IN, CHECK_OUT, GUESTS);

            // when
            RateFetchCriteria criteria = factory.create(query);

            // then
            assertThat(criteria.stayDates()).hasSize(2);
            assertThat(criteria.stayDates()).containsExactly(
                LocalDate.of(2026, 4, 10),
                LocalDate.of(2026, 4, 11)
            );
        }
    }
}
