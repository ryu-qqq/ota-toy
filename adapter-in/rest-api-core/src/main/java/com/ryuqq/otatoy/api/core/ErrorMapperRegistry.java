package com.ryuqq.otatoy.api.core;

import com.ryuqq.otatoy.domain.common.DomainException;
import com.ryuqq.otatoy.domain.common.ErrorCategory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 등록된 ErrorMapper 구현체를 순회하며 DomainException에 적합한 매퍼를 찾아 변환한다.
 * <p>
 * 매칭되는 매퍼가 없으면 에러 카테고리 기반의 기본 매핑을 적용한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Component
public class ErrorMapperRegistry {

    private final List<ErrorMapper> mappers;

    public ErrorMapperRegistry(List<ErrorMapper> mappers) {
        this.mappers = mappers;
    }

    /**
     * DomainException에 적합한 매퍼를 찾아 MappedError를 반환한다.
     * 매칭 매퍼가 없으면 기본 매핑을 적용한다.
     */
    public ErrorMapper.MappedError resolve(DomainException ex) {
        return mappers.stream()
            .filter(m -> m.supports(ex))
            .findFirst()
            .map(m -> m.map(ex))
            .orElse(defaultMapping(ex));
    }

    private ErrorMapper.MappedError defaultMapping(DomainException ex) {
        String code = ex.code();
        String message = ex.errorMessage();
        HttpStatus status = resolveStatusByCategory(ex.category());
        return new ErrorMapper.MappedError(status, code, message, message);
    }

    private HttpStatus resolveStatusByCategory(ErrorCategory category) {
        return switch (category) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case CONFLICT -> HttpStatus.CONFLICT;
            case FORBIDDEN -> HttpStatus.UNPROCESSABLE_ENTITY;
        };
    }
}
