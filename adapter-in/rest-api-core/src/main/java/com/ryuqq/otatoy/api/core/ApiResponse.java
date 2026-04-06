package com.ryuqq.otatoy.api.core;

import org.slf4j.MDC;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 모든 API의 공통 성공 응답 래퍼.
 * 에러 응답은 RFC 7807 ProblemDetail로 별도 처리한다.
 *
 * @param <T> 응답 데이터 타입
 * @author ryu-qqq
 * @since 2026-04-05
 */
public record ApiResponse<T>(
    T data,
    String timestamp,
    String requestId
) {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
        .ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneId.of("Asia/Seoul"));

    /**
     * 데이터를 포함한 성공 응답을 생성한다.
     */
    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data, formatNow(), resolveRequestId());
    }

    /**
     * 데이터 없이 성공 응답을 생성한다.
     */
    public static ApiResponse<Void> of() {
        return new ApiResponse<>(null, formatNow(), resolveRequestId());
    }

    private static String formatNow() {
        return FORMATTER.format(Instant.now());
    }

    private static String resolveRequestId() {
        String traceId = MDC.get("traceId");
        return traceId != null ? traceId : UUID.randomUUID().toString();
    }
}
