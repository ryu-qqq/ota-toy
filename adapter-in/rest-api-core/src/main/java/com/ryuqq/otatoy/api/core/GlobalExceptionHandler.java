package com.ryuqq.otatoy.api.core;

import com.ryuqq.otatoy.domain.common.DomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 전역 예외 처리기.
 * Controller에서 try-catch를 사용하지 않고 이 클래스에서 일괄 처리한다.
 * ErrorMapperRegistry를 통해 DomainException을 적절한 HTTP 응답으로 변환한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ErrorMapperRegistry errorMapperRegistry;

    public GlobalExceptionHandler(ErrorMapperRegistry errorMapperRegistry) {
        this.errorMapperRegistry = errorMapperRegistry;
    }

    /**
     * DomainException 처리 -- ErrorMapperRegistry로 변환하여 응답한다.
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException e) {
        ErrorMapper.MappedError mapped = errorMapperRegistry.resolve(e);

        log.warn("도메인 예외 발생: {} - {}", mapped.code(), mapped.debugMessage());

        return ResponseEntity
            .status(mapped.status())
            .body(ApiResponse.error(mapped.code(), mapped.userMessage(), mapped.debugMessage()));
    }

    /**
     * Jakarta Validation 실패 처리 -- 필드별 에러 메시지를 수집하여 응답한다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException e) {

        String userMessage = "입력값이 올바르지 않습니다";
        String debugMessage = e.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .collect(Collectors.joining(", "));

        log.warn("검증 실패: {}", debugMessage);

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("VALIDATION_ERROR", userMessage, debugMessage));
    }

    /**
     * 예상치 못한 예외 처리 -- 500 응답.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception e) {
        log.error("예상치 못한 예외 발생", e);

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(
                "INTERNAL_ERROR",
                "서버 내부 오류가 발생했습니다",
                e.getMessage()
            ));
    }
}
