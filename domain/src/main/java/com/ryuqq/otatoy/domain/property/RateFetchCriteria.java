package com.ryuqq.otatoy.domain.property;

import java.time.LocalDate;
import java.util.List;

/**
 * 숙소 상세 페이지에서 요금을 조회하기 위한 조건.
 * 특정 숙소의 날짜별 객실 요금 + 재고를 반환하는 데 사용한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record RateFetchCriteria(
        PropertyId propertyId,
        LocalDate checkIn,
        LocalDate checkOut,
        int guests
) {

    public RateFetchCriteria {
        if (propertyId == null) {
            throw new IllegalArgumentException("숙소 ID는 필수입니다");
        }
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("체크인/체크아웃 날짜는 필수입니다");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("체크아웃은 체크인보다 뒤여야 합니다");
        }
        if (guests < 1) {
            throw new IllegalArgumentException("투숙 인원은 1명 이상이어야 합니다");
        }
    }

    /**
     * 숙박 기간의 모든 날짜 목록 (체크아웃 제외).
     */
    public List<LocalDate> stayDates() {
        return checkIn.datesUntil(checkOut).toList();
    }

    /**
     * 숙박 일수.
     */
    public long nights() {
        return java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
    }
}
