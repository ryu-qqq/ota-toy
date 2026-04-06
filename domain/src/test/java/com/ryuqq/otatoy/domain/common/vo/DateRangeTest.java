package com.ryuqq.otatoy.domain.common.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateRangeTest {

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("유효한 날짜 범위로 생성할 수 있다")
        void shouldCreateWithValidRange() {
            LocalDate start = LocalDate.of(2026, 4, 1);
            LocalDate end = LocalDate.of(2026, 4, 5);

            DateRange range = new DateRange(start, end);

            assertThat(range.startDate()).isEqualTo(start);
            assertThat(range.endDate()).isEqualTo(end);
        }

        @Test
        @DisplayName("시작일이 null이면 예외가 발생한다")
        void shouldThrowWhenStartDateNull() {
            assertThatThrownBy(() -> new DateRange(null, LocalDate.of(2026, 4, 5)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("시작일과 종료일은 필수");
        }

        @Test
        @DisplayName("종료일이 null이면 예외가 발생한다")
        void shouldThrowWhenEndDateNull() {
            assertThatThrownBy(() -> new DateRange(LocalDate.of(2026, 4, 1), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("시작일과 종료일은 필수");
        }

        @Test
        @DisplayName("종료일이 시작일과 같으면 예외가 발생한다")
        void shouldThrowWhenSameDate() {
            LocalDate date = LocalDate.of(2026, 4, 1);
            assertThatThrownBy(() -> new DateRange(date, date))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("종료일은 시작일보다 뒤");
        }

        @Test
        @DisplayName("종료일이 시작일보다 앞이면 예외가 발생한다")
        void shouldThrowWhenEndBeforeStart() {
            assertThatThrownBy(() -> new DateRange(
                    LocalDate.of(2026, 4, 5),
                    LocalDate.of(2026, 4, 1)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("종료일은 시작일보다 뒤");
        }
    }

    @Nested
    @DisplayName("도메인 로직 검증")
    class DomainLogic {

        @Test
        @DisplayName("nights()는 숙박 일수를 반환한다")
        void shouldReturnNights() {
            DateRange range = new DateRange(
                    LocalDate.of(2026, 4, 1),
                    LocalDate.of(2026, 4, 5));

            assertThat(range.nights()).isEqualTo(4);
        }

        @Test
        @DisplayName("1박 범위의 nights()는 1을 반환한다")
        void shouldReturnOneNight() {
            DateRange range = new DateRange(
                    LocalDate.of(2026, 4, 1),
                    LocalDate.of(2026, 4, 2));

            assertThat(range.nights()).isEqualTo(1);
        }

        @Test
        @DisplayName("dates()는 시작일부터 종료일 전날까지 스트림을 반환한다")
        void shouldReturnDatesStream() {
            DateRange range = new DateRange(
                    LocalDate.of(2026, 4, 1),
                    LocalDate.of(2026, 4, 4));

            List<LocalDate> dates = range.dates().toList();

            assertThat(dates).containsExactly(
                    LocalDate.of(2026, 4, 1),
                    LocalDate.of(2026, 4, 2),
                    LocalDate.of(2026, 4, 3));
        }
    }

    @Nested
    @DisplayName("동등성 검증")
    class Equality {

        @Test
        @DisplayName("같은 날짜 범위는 동등하다")
        void sameValueShouldBeEqual() {
            DateRange a = new DateRange(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 5));
            DateRange b = new DateRange(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 5));
            assertThat(a).isEqualTo(b);
        }
    }
}
