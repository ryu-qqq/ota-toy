package com.ryuqq.otatoy.api.core;

import com.ryuqq.otatoy.domain.common.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * RFC 7807 (ProblemDetail) 기반 전역 예외 처리기.
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
     * DomainException 처리 -- ErrorMapperRegistry로 변환하여 ProblemDetail 응답한다.
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ProblemDetail> handleDomainException(DomainException e, HttpServletRequest request) {
        ErrorMapper.MappedError mapped = errorMapperRegistry.resolve(e);

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(mapped.status(), mapped.detail());
        pd.setTitle(mapped.title());
        pd.setType(URI.create("about:blank"));
        pd.setInstance(URI.create(request.getRequestURI()));
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("code", mapped.code());
        pd.setProperty("traceId", resolveTraceId());

        if (mapped.status().is5xxServerError()) {
            log.error("DomainException (Server Error): code={}, detail={}", mapped.code(), mapped.detail(), e);
        } else if (mapped.status() == HttpStatus.NOT_FOUND) {
            log.debug("DomainException (Not Found): code={}, detail={}", mapped.code(), mapped.detail());
        } else {
            log.warn("DomainException (Client Error): code={}, detail={}", mapped.code(), mapped.detail());
        }

        return ResponseEntity.status(mapped.status())
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .header("x-error-code", mapped.code())
            .body(pd);
    }

    /**
     * Jakarta Validation 실패 처리 -- 필드별 에러 메시지를 ProblemDetail로 응답한다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        Map<String, String> errors = e.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid",
                (a, b) -> a
            ));

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다");
        pd.setTitle("Validation Failed");
        pd.setType(URI.create("about:blank"));
        pd.setInstance(URI.create(request.getRequestURI()));
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("code", "VALIDATION_FAILED");
        pd.setProperty("errors", errors);
        pd.setProperty("traceId", resolveTraceId());

        log.warn("검증 실패: {}", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .header("x-error-code", "VALIDATION_FAILED")
            .body(pd);
    }

    /**
     * BindException 처리 -- Query Parameter 바인딩 실패.
     */
    @ExceptionHandler(org.springframework.validation.BindException.class)
    public ResponseEntity<ProblemDetail> handleBindException(org.springframework.validation.BindException e, HttpServletRequest request) {
        Map<String, String> errors = e.getFieldErrors().stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid",
                (a, b) -> a
            ));

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "바인딩 오류가 발생했습니다");
        pd.setTitle("Binding Failed");
        pd.setType(URI.create("about:blank"));
        pd.setInstance(URI.create(request.getRequestURI()));
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("code", "BINDING_FAILED");
        pd.setProperty("errors", errors);
        pd.setProperty("traceId", resolveTraceId());

        log.warn("바인딩 실패: {}", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .header("x-error-code", "BINDING_FAILED")
            .body(pd);
    }

    /**
     * 필수 요청 파라미터 누락 처리.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingParameter(MissingServletRequestParameterException e, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        pd.setTitle("Missing Parameter");
        pd.setType(URI.create("about:blank"));
        pd.setInstance(URI.create(request.getRequestURI()));
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("code", "MISSING_PARAMETER");
        pd.setProperty("traceId", resolveTraceId());

        log.warn("필수 파라미터 누락: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .header("x-error-code", "MISSING_PARAMETER")
            .body(pd);
    }

    /**
     * 요청 본문 파싱 실패 처리.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleNotReadable(HttpMessageNotReadableException e, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "요청 본문을 파싱할 수 없습니다");
        pd.setTitle("Invalid Format");
        pd.setType(URI.create("about:blank"));
        pd.setInstance(URI.create(request.getRequestURI()));
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("code", "INVALID_FORMAT");
        pd.setProperty("traceId", resolveTraceId());

        log.warn("요청 본문 파싱 실패: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .header("x-error-code", "INVALID_FORMAT")
            .body(pd);
    }

    /**
     * 타입 미스매치 처리.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String detail = String.format("파라미터 '%s'의 타입이 올바르지 않습니다", e.getName());

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        pd.setTitle("Type Mismatch");
        pd.setType(URI.create("about:blank"));
        pd.setInstance(URI.create(request.getRequestURI()));
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("code", "TYPE_MISMATCH");
        pd.setProperty("traceId", resolveTraceId());

        log.warn("타입 미스매치: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .header("x-error-code", "TYPE_MISMATCH")
            .body(pd);
    }

    /**
     * 지원하지 않는 HTTP 메서드 처리.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.METHOD_NOT_ALLOWED, e.getMessage());
        pd.setTitle("Method Not Allowed");
        pd.setType(URI.create("about:blank"));
        pd.setInstance(URI.create(request.getRequestURI()));
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("code", "METHOD_NOT_ALLOWED");
        pd.setProperty("traceId", resolveTraceId());

        log.warn("지원하지 않는 HTTP 메서드: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .header("x-error-code", "METHOD_NOT_ALLOWED")
            .body(pd);
    }

    /**
     * 필수 요청 헤더 누락 처리 (Idempotency-Key 등).
     */
    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<ProblemDetail> handleServletRequestBinding(ServletRequestBindingException e, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        pd.setTitle("Missing Header or Cookie");
        pd.setType(URI.create("about:blank"));
        pd.setInstance(URI.create(request.getRequestURI()));
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("code", "MISSING_HEADER");
        pd.setProperty("traceId", resolveTraceId());

        log.warn("요청 바인딩 실패: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .header("x-error-code", "MISSING_HEADER")
            .body(pd);
    }

    /**
     * 잘못된 인자 처리 (날짜/시간 파싱 실패, Enum 변환 실패, 비즈니스 검증 실패 등).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        pd.setTitle("Invalid Argument");
        pd.setType(URI.create("about:blank"));
        pd.setInstance(URI.create(request.getRequestURI()));
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("code", "INVALID_ARGUMENT");
        pd.setProperty("traceId", resolveTraceId());

        log.warn("잘못된 인자: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .header("x-error-code", "INVALID_ARGUMENT")
            .body(pd);
    }

    /**
     * DateTimeParseException 처리 (시간/날짜 포맷 오류).
     */
    @ExceptionHandler(java.time.format.DateTimeParseException.class)
    public ResponseEntity<ProblemDetail> handleDateTimeParse(java.time.format.DateTimeParseException e, HttpServletRequest request) {
        String detail = String.format("날짜/시간 형식이 올바르지 않습니다: %s", e.getParsedString());

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        pd.setTitle("Invalid DateTime Format");
        pd.setType(URI.create("about:blank"));
        pd.setInstance(URI.create(request.getRequestURI()));
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("code", "INVALID_DATETIME_FORMAT");
        pd.setProperty("traceId", resolveTraceId());

        log.warn("날짜/시간 파싱 실패: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .header("x-error-code", "INVALID_DATETIME_FORMAT")
            .body(pd);
    }

    /**
     * 예상치 못한 예외 처리 -- 500 응답.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpectedException(Exception e, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다");
        pd.setTitle("Internal Server Error");
        pd.setType(URI.create("about:blank"));
        pd.setInstance(URI.create(request.getRequestURI()));
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("code", "INTERNAL_ERROR");
        pd.setProperty("traceId", resolveTraceId());

        log.error("예상치 못한 예외 발생", e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .header("x-error-code", "INTERNAL_ERROR")
            .body(pd);
    }

    private String resolveTraceId() {
        String traceId = MDC.get("traceId");
        return traceId != null ? traceId : UUID.randomUUID().toString();
    }
}
