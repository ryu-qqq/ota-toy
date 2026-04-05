package com.ryuqq.otatoy.domain.property;

import com.ryuqq.otatoy.domain.accommodation.AmenityType;
import com.ryuqq.otatoy.domain.common.sort.SortDirection;
import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;

import java.time.LocalDate;
import java.util.List;

/**
 * 고객 숙소 검색 조건.
 * Booking.com, Trip.com 등 실제 OTA 플랫폼의 검색 필터를 분석하여 도출했다.
 *
 * <p>필수 조건: checkIn, checkOut, guests, size
 * <p>선택 조건: keyword, region, propertyTypeId, minPrice, maxPrice, amenityTypes,
 *             freeCancellationOnly, starRating
 *
 * @author ryu-qqq
 * @since 2026-04-06
 * @see PropertySortKey 정렬 키
 */
public record PropertySliceCriteria(
        String keyword,
        String region,
        PropertyTypeId propertyTypeId,
        LocalDate checkIn,
        LocalDate checkOut,
        int guests,
        Money minPrice,
        Money maxPrice,
        List<AmenityType> amenityTypes,
        boolean freeCancellationOnly,
        Integer starRating,
        PropertySortKey sortKey,
        SortDirection direction,
        int size,
        Long cursor
) {

    public PropertySliceCriteria {
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("체크인/체크아웃 날짜는 필수입니다");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("체크아웃은 체크인보다 뒤여야 합니다");
        }
        if (guests < 1) {
            throw new IllegalArgumentException("투숙 인원은 1명 이상이어야 합니다");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("페이지 크기는 1~100이어야 합니다");
        }
        if (starRating != null && (starRating < 1 || starRating > 5)) {
            throw new IllegalArgumentException("성급은 1~5 사이여야 합니다");
        }
        if (minPrice != null && maxPrice != null && minPrice.isGreaterThan(maxPrice)) {
            throw new IllegalArgumentException("최소 가격이 최대 가격보다 클 수 없습니다");
        }
        if (amenityTypes == null) {
            amenityTypes = List.of();
        }
        if (sortKey == null) {
            sortKey = PropertySortKey.PRICE_LOW;
        }
        if (direction == null) {
            direction = SortDirection.ASC;
        }
    }

    /**
     * 숙박 기간의 모든 날짜 목록 (체크아웃 제외).
     * 재고 확인 및 요금 조회에 사용한다.
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
