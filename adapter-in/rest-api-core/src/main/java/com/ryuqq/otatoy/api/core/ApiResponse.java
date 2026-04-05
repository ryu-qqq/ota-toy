package com.ryuqq.otatoy.api.core;

/**
 * 모든 API의 공통 응답 래퍼.
 * 성공/실패 여부를 {@code success} 필드로 구분하고,
 * 에러 시에는 {@link ErrorDetail}로 코드/메시지를 전달한다.
 *
 * @param <T> 응답 데이터 타입
 * @author ryu-qqq
 * @since 2026-04-05
 */
public record ApiResponse<T>(
    boolean success,
    T data,
    ErrorDetail error
) {

    /**
     * 성공 응답을 생성한다.
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /**
     * 에러 응답을 생성한다.
     */
    public static ApiResponse<Void> error(String code, String userMessage, String debugMessage) {
        return new ApiResponse<>(false, null, new ErrorDetail(code, userMessage, debugMessage));
    }

    /**
     * {@link ErrorDetail}을 직접 받아 에러 응답을 생성한다.
     */
    public static ApiResponse<Void> error(ErrorDetail errorDetail) {
        return new ApiResponse<>(false, null, errorDetail);
    }

    /**
     * 에러 상세 정보.
     *
     * @param code         도메인 에러 코드 (예: "INV-001")
     * @param userMessage  사용자에게 노출하는 메시지
     * @param debugMessage 내부 로깅용 상세 메시지
     */
    public record ErrorDetail(
        String code,
        String userMessage,
        String debugMessage
    ) {}
}
