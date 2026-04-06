package com.ryuqq.otatoy.api.core.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * HTTP 요청/응답 로깅 필터.
 * 요청 시작 시 traceId를 MDC에 설정하고, 요청 완료 시 HTTP 메서드, URI, 상태 코드, 처리 시간을 로깅한다.
 *
 * <p>보안 및 성능을 위해 요청/응답 body는 로깅하지 않는다 (PII 위험, 성능 영향).</p>
 * <p>Swagger, API 문서, Actuator 경로는 로깅에서 제외한다.</p>
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        long start = System.currentTimeMillis();

        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            int status = response.getStatus();

            if (status >= 500) {
                log.error("[{}] {} {} -> {} ({}ms)", traceId, request.getMethod(), request.getRequestURI(), status, duration);
            } else if (status >= 400) {
                log.warn("[{}] {} {} -> {} ({}ms)", traceId, request.getMethod(), request.getRequestURI(), status, duration);
            } else {
                log.info("[{}] {} {} -> {} ({}ms)", traceId, request.getMethod(), request.getRequestURI(), status, duration);
            }

            MDC.clear();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/swagger")
                || path.startsWith("/api-docs")
                || path.startsWith("/actuator");
    }
}
