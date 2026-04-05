package com.ryuqq.otatoy.api.extranet.common.error;

import com.ryuqq.otatoy.api.core.ErrorMapper;
import com.ryuqq.otatoy.domain.common.DomainException;
import com.ryuqq.otatoy.domain.common.ErrorCategory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Extranet 숙소 관련 DomainException을 HTTP 응답으로 매핑하는 구현체.
 * ACC-(숙소), PTN-(파트너), PT-(숙소유형) 접두사 에러 코드를 처리한다.
 * ErrorCategory 기반으로 HTTP 상태를 결정한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class ExtranetPropertyErrorMapper implements ErrorMapper {

    @Override
    public boolean supports(DomainException ex) {
        String code = ex.getErrorCode().getCode();
        return code.startsWith("ACC-") || code.startsWith("PTN-") || code.startsWith("PT-");
    }

    @Override
    public MappedError map(DomainException ex) {
        String code = ex.getErrorCode().getCode();
        String message = ex.getErrorCode().getMessage();
        ErrorCategory category = ex.getErrorCode().getCategory();
        HttpStatus status = resolveStatus(category);
        return new MappedError(status, code, message, message);
    }

    private HttpStatus resolveStatus(ErrorCategory category) {
        return switch (category) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case CONFLICT -> HttpStatus.CONFLICT;
            case FORBIDDEN -> HttpStatus.UNPROCESSABLE_ENTITY;
        };
    }
}
