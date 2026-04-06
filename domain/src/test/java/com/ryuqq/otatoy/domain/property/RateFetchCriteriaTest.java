package com.ryuqq.otatoy.domain.property;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * RateFetchCriteria 생성 검증 및 도메인 로직 테스트.
 */
class RateFetchCriteriaTest {

    private static final PropertyId PROPERTY_ID = PropertyId.of(1L);
    private static final LocalDate CHECK_IN = LocalDate.of(2026, 4, 10);
    private static final LocalDate CHECK_OUT = LocalDate.of(2026, 4, 13);

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("정상 생성")
        void shouldCreateSuccessfully() {
            RateFetchCriteria criteria = new RateFetchCriteria(PROPERTY_ID, CHECK_IN, CHECK_OUT, 2);
            assertThat(criteria.propertyId()).isEqualTo(PROPERTY_ID);
            assertThat(criteria.checkIn()).isEqualTo(CHECK_IN);
            assertThat(criteria.checkOut()).isEqualTo(CHECK_OUT);
            assertThat(criteria.guests()).isEqualTo(2);
        }

        @Test
        @DisplayName("propertyId가 null이면 생성 실패")
        void shouldFailWhenPropertyIdIsNull() {
            assertThatThrownBy(() -> new RateFetchCriteria(null, CHECK_IN, CHECK_OUT, 2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("숙소 ID는 필수");
        }

        @Test
        @DisplayName("checkIn이 null이면 생성 실패")
        void shouldFailWhenCheckInIsNull() {
            assertThatThrownBy(() -> new RateFetchCriteria(PROPERTY_ID, null, CHECK_OUT, 2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("체크인/체크아웃 날짜는 필수");
        }

        @Test
        @DisplayName("checkOut이 null이면 생성 실패")
        void shouldFailWhenCheckOutIsNull() {
            assertThatThrownBy(() -> new RateFetchCriteria(PROPERTY_ID, CHECK_IN, null, 2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("체크인/체크아웃 날짜는 필수");
        }

        @Test
        @DisplayName("checkOut이 checkIn과 같으면 생성 실패")
        void shouldFailWhenCheckOutEqualsCheckIn() {
            assertThatThrownBy(() -> new RateFetchCriteria(PROPERTY_ID, CHECK_IN, CHECK_IN, 2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("체크아웃은 체크인보다 뒤");
        }

        @Test
        @DisplayName("checkOut이 checkIn보다 앞이면 생성 실패")
        void shouldFailWhenCheckOutBeforeCheckIn() {
            assertThatThrownBy(() -> new RateFetchCriteria(PROPERTY_ID, CHECK_OUT, CHECK_IN, 2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("체크아웃은 체크인보다 뒤");
        }

        @Test
        @DisplayName("guests가 0이면 생성 실패")
        void shouldFailWhenGuestsIsZero() {
            assertThatThrownBy(() -> new RateFetchCriteria(PROPERTY_ID, CHECK_IN, CHECK_OUT, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("투숙 인원은 1명 이상");
        }

        @Test
        @DisplayName("guests가 음수이면 생성 실패")
        void shouldFailWhenGuestsIsNegative() {
            assertThatThrownBy(() -> new RateFetchCriteria(PROPERTY_ID, CHECK_IN, CHECK_OUT, -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("투숙 인원은 1명 이상");
        }
    }

    @Nested
    @DisplayName("도메인 로직")
    class DomainLogic {

        @Test
        @DisplayName("stayDates()는 체크인~체크아웃 전날까지 반환한다")
        void shouldReturnStayDates() {
            RateFetchCriteria criteria = new RateFetchCriteria(PROPERTY_ID, CHECK_IN, CHECK_OUT, 2);
            List<LocalDate> stayDates = criteria.stayDates();
            assertThat(stayDates).hasSize(3);
            assertThat(stayDates).containsExactly(
                    LocalDate.of(2026, 4, 10),
                    LocalDate.of(2026, 4, 11),
                    LocalDate.of(2026, 4, 12)
            );
        }

        @Test
        @DisplayName("nights()는 숙박 일수를 반환한다")
        void shouldReturnNights() {
            RateFetchCriteria criteria = new RateFetchCriteria(PROPERTY_ID, CHECK_IN, CHECK_OUT, 2);
            assertThat(criteria.nights()).isEqualTo(3);
        }
    }
}
