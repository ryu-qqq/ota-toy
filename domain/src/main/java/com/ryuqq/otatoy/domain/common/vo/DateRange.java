package com.ryuqq.otatoy.domain.common.vo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

public record DateRange(LocalDate startDate, LocalDate endDate) {

    public DateRange {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("시작일과 종료일은 필수입니다");
        }
        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("종료일은 시작일보다 뒤여야 합니다");
        }
    }

    public long nights() {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    public Stream<LocalDate> dates() {
        return startDate.datesUntil(endDate);
    }
}
