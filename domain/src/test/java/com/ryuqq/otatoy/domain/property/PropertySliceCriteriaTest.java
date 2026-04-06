package com.ryuqq.otatoy.domain.property;

import com.ryuqq.otatoy.domain.common.sort.SortDirection;
import com.ryuqq.otatoy.domain.common.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PropertySliceCriteria 생성 검증 및 도메인 로직 테스트.
 */
class PropertySliceCriteriaTest {

    private static final LocalDate CHECK_IN = LocalDate.of(2026, 4, 10);
    private static final LocalDate CHECK_OUT = LocalDate.of(2026, 4, 13);

    private PropertySliceCriteria validCriteria() {
        return new PropertySliceCriteria(
                null, null, null, CHECK_IN, CHECK_OUT, 2,
                null, null, null, false, null,
                null, null, 20, null
        );
    }

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("정상 생성 -- 기본값 적용")
        void shouldCreateWithDefaults() {
            PropertySliceCriteria criteria = validCriteria();

            assertThat(criteria.checkIn()).isEqualTo(CHECK_IN);
            assertThat(criteria.checkOut()).isEqualTo(CHECK_OUT);
            assertThat(criteria.guests()).isEqualTo(2);
            assertThat(criteria.size()).isEqualTo(20);
            // 기본값 적용 확인
            assertThat(criteria.amenityTypes()).isEmpty();
            assertThat(criteria.sortKey()).isEqualTo(PropertySortKey.PRICE_LOW);
            assertThat(criteria.direction()).isEqualTo(SortDirection.ASC);
        }

        @Test
        @DisplayName("checkIn이 null이면 생성 실패")
        void shouldFailWhenCheckInIsNull() {
            assertThatThrownBy(() -> new PropertySliceCriteria(
                    null, null, null, null, CHECK_OUT, 2,
                    null, null, null, false, null,
                    null, null, 20, null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("체크인/체크아웃 날짜는 필수");
        }

        @Test
        @DisplayName("checkOut이 null이면 생성 실패")
        void shouldFailWhenCheckOutIsNull() {
            assertThatThrownBy(() -> new PropertySliceCriteria(
                    null, null, null, CHECK_IN, null, 2,
                    null, null, null, false, null,
                    null, null, 20, null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("체크인/체크아웃 날짜는 필수");
        }

        @Test
        @DisplayName("checkOut이 checkIn과 같으면 생성 실패")
        void shouldFailWhenCheckOutEqualsCheckIn() {
            assertThatThrownBy(() -> new PropertySliceCriteria(
                    null, null, null, CHECK_IN, CHECK_IN, 2,
                    null, null, null, false, null,
                    null, null, 20, null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("체크아웃은 체크인보다 뒤");
        }

        @Test
        @DisplayName("checkOut이 checkIn보다 앞이면 생성 실패")
        void shouldFailWhenCheckOutBeforeCheckIn() {
            assertThatThrownBy(() -> new PropertySliceCriteria(
                    null, null, null, CHECK_OUT, CHECK_IN, 2,
                    null, null, null, false, null,
                    null, null, 20, null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("체크아웃은 체크인보다 뒤");
        }

        @Test
        @DisplayName("guests가 0이면 생성 실패")
        void shouldFailWhenGuestsIsZero() {
            assertThatThrownBy(() -> new PropertySliceCriteria(
                    null, null, null, CHECK_IN, CHECK_OUT, 0,
                    null, null, null, false, null,
                    null, null, 20, null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("투숙 인원은 1명 이상");
        }

        @Test
        @DisplayName("size가 0이면 생성 실패")
        void shouldFailWhenSizeIsZero() {
            assertThatThrownBy(() -> new PropertySliceCriteria(
                    null, null, null, CHECK_IN, CHECK_OUT, 2,
                    null, null, null, false, null,
                    null, null, 0, null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("페이지 크기는 1~100");
        }

        @Test
        @DisplayName("size가 101이면 생성 실패")
        void shouldFailWhenSizeExceeds100() {
            assertThatThrownBy(() -> new PropertySliceCriteria(
                    null, null, null, CHECK_IN, CHECK_OUT, 2,
                    null, null, null, false, null,
                    null, null, 101, null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("페이지 크기는 1~100");
        }

        @Test
        @DisplayName("경계값 -- size=1, size=100은 성공")
        void shouldSucceedAtBoundarySize() {
            PropertySliceCriteria c1 = new PropertySliceCriteria(
                    null, null, null, CHECK_IN, CHECK_OUT, 1,
                    null, null, null, false, null,
                    null, null, 1, null
            );
            assertThat(c1.size()).isEqualTo(1);

            PropertySliceCriteria c100 = new PropertySliceCriteria(
                    null, null, null, CHECK_IN, CHECK_OUT, 1,
                    null, null, null, false, null,
                    null, null, 100, null
            );
            assertThat(c100.size()).isEqualTo(100);
        }

        @Test
        @DisplayName("starRating이 0이면 생성 실패")
        void shouldFailWhenStarRatingIsZero() {
            assertThatThrownBy(() -> new PropertySliceCriteria(
                    null, null, null, CHECK_IN, CHECK_OUT, 2,
                    null, null, null, false, 0,
                    null, null, 20, null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("성급은 1~5 사이");
        }

        @Test
        @DisplayName("starRating이 6이면 생성 실패")
        void shouldFailWhenStarRatingIs6() {
            assertThatThrownBy(() -> new PropertySliceCriteria(
                    null, null, null, CHECK_IN, CHECK_OUT, 2,
                    null, null, null, false, 6,
                    null, null, 20, null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("성급은 1~5 사이");
        }

        @Test
        @DisplayName("starRating null이면 성공 (선택)")
        void shouldAllowNullStarRating() {
            PropertySliceCriteria criteria = validCriteria();
            assertThat(criteria.starRating()).isNull();
        }

        @Test
        @DisplayName("minPrice가 maxPrice보다 크면 생성 실패")
        void shouldFailWhenMinPriceGreaterThanMaxPrice() {
            assertThatThrownBy(() -> new PropertySliceCriteria(
                    null, null, null, CHECK_IN, CHECK_OUT, 2,
                    Money.of(100000), Money.of(50000), null, false, null,
                    null, null, 20, null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("최소 가격이 최대 가격보다 클 수 없습니다");
        }

        @Test
        @DisplayName("minPrice가 maxPrice와 같으면 성공")
        void shouldSucceedWhenMinPriceEqualsMaxPrice() {
            PropertySliceCriteria criteria = new PropertySliceCriteria(
                    null, null, null, CHECK_IN, CHECK_OUT, 2,
                    Money.of(100000), Money.of(100000), null, false, null,
                    null, null, 20, null
            );
            assertThat(criteria.minPrice()).isEqualTo(Money.of(100000));
        }
    }

    @Nested
    @DisplayName("도메인 로직")
    class DomainLogic {

        @Test
        @DisplayName("stayDates()는 체크인~체크아웃 전날까지의 날짜 목록을 반환한다")
        void shouldReturnStayDates() {
            PropertySliceCriteria criteria = validCriteria();

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
            PropertySliceCriteria criteria = validCriteria();
            assertThat(criteria.nights()).isEqualTo(3);
        }

        @Test
        @DisplayName("1박일 경우 stayDates는 1개, nights는 1")
        void shouldHandleOneNight() {
            PropertySliceCriteria criteria = new PropertySliceCriteria(
                    null, null, null,
                    LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 11), 1,
                    null, null, null, false, null,
                    null, null, 20, null
            );
            assertThat(criteria.stayDates()).hasSize(1);
            assertThat(criteria.nights()).isEqualTo(1);
        }
    }
}
