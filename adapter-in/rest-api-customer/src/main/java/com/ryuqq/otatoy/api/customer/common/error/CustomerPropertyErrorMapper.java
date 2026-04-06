package com.ryuqq.otatoy.api.customer.common.error;

import com.ryuqq.otatoy.api.core.ErrorMapper;
import com.ryuqq.otatoy.domain.common.DomainException;
import com.ryuqq.otatoy.domain.common.ErrorCategory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Customer 숙소/요금 관련 DomainException을 HTTP 응답으로 매핑하는 구현체.
 * ACC-(숙소), PRC-(요금정책), RT-(객실) 접두사 에러 코드를 처리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class CustomerPropertyErrorMapper implements ErrorMapper {

    @Override
    public boolean supports(DomainException ex) {
        String code = ex.code();
        return code.startsWith("ACC-") || code.startsWith("PRC-") || code.startsWith("RT-");
    }

    @Override
    public MappedError map(DomainException ex) {
        ErrorCategory category = ex.category();
        HttpStatus status = switch (category) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case CONFLICT -> HttpStatus.CONFLICT;
            case FORBIDDEN -> HttpStatus.UNPROCESSABLE_ENTITY;
        };

        return new MappedError(
            status,
            ex.code(),
            ex.errorMessage(),
            ex.errorMessage()
        );
    }
}
