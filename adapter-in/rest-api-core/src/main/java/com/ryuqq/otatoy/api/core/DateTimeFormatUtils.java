package com.ryuqq.otatoy.api.core;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 날짜/시간 포맷 변환 유틸리티.
 * API 응답에서 Instant, LocalDate, LocalTime을 문자열로 변환할 때 사용한다.
 * KST(Asia/Seoul) 기준으로 포맷한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public final class DateTimeFormatUtils {

    private static final DateTimeFormatter KST_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    private DateTimeFormatUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Instant를 "yyyy-MM-dd HH:mm:ss" (KST) 형식으로 변환한다.
     */
    public static String formatDateTime(Instant instant) {
        if (instant == null) return null;
        return instant.atZone(ZONE_ID).format(KST_FORMATTER);
    }

    /**
     * LocalDate를 "yyyy-MM-dd" 형식으로 변환한다.
     */
    public static String formatDate(LocalDate date) {
        if (date == null) return null;
        return date.format(DATE_FORMATTER);
    }

    /**
     * LocalTime을 "HH:mm" 형식으로 변환한다.
     */
    public static String formatTime(LocalTime time) {
        if (time == null) return null;
        return time.format(TIME_FORMATTER);
    }

    /**
     * 현재 시각을 "yyyy-MM-dd HH:mm:ss" (KST) 형식으로 반환한다.
     */
    public static String now() {
        return formatDateTime(Instant.now());
    }
}
