package com.ryuqq.otatoy.api.customer.reservation;

import com.ryuqq.otatoy.api.core.ErrorMapper;
import com.ryuqq.otatoy.domain.common.DomainException;
import com.ryuqq.otatoy.domain.common.ErrorCategory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 예약/재고 관련 DomainException을 에러 응답으로 변환하는 매퍼.
 * RSV-(예약), INV-(재고) 접두사 에러를 처리한다.
 * HTTP 상태 결정은 ErrorCategory 기반으로만 수행한다 (메시지/코드 패턴 의존 금지).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class CustomerReservationErrorMapper implements ErrorMapper {

    private static final Map<ErrorCategory, HttpStatus> CATEGORY_STATUS_MAP = Map.of(
        ErrorCategory.NOT_FOUND,   HttpStatus.NOT_FOUND,
        ErrorCategory.VALIDATION,  HttpStatus.BAD_REQUEST,
        ErrorCategory.CONFLICT,    HttpStatus.CONFLICT,
        ErrorCategory.FORBIDDEN,   HttpStatus.UNPROCESSABLE_ENTITY
    );

    @Override
    public boolean supports(DomainException ex) {
        String code = ex.getErrorCode().getCode();
        return code.startsWith("RSV-") || code.startsWith("INV-");
    }

    @Override
    public MappedError map(DomainException ex) {
        ErrorCategory category = ex.getErrorCode().getCategory();
        HttpStatus status = CATEGORY_STATUS_MAP.getOrDefault(category, HttpStatus.INTERNAL_SERVER_ERROR);

        String debugMessage = ex.getArgs().isEmpty()
            ? ex.getErrorCode().getMessage()
            : ex.getArgs().toString();

        return new MappedError(
            status,
            ex.getErrorCode().getCode(),
            ex.getErrorCode().getMessage(),
            debugMessage
        );
    }
}
