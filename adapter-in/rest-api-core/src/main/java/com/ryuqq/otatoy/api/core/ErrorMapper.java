package com.ryuqq.otatoy.api.core;

import com.ryuqq.otatoy.domain.common.DomainException;
import org.springframework.http.HttpStatus;

/**
 * DomainException을 API 에러 응답으로 변환하는 매퍼 인터페이스.
 * <p>
 * 각 API 모듈(extranet, customer, admin)에서 도메인별 매핑 구현체를 제공한다.
 * ErrorMapperRegistry가 등록된 매퍼를 순회하며 적절한 매퍼를 선택한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public interface ErrorMapper {

    /**
     * 이 매퍼가 주어진 DomainException을 처리할 수 있는지 판단한다.
     */
    boolean supports(DomainException ex);

    /**
     * DomainException을 MappedError로 변환한다.
     */
    MappedError map(DomainException ex);

    /**
     * 매핑 결과를 담는 레코드.
     *
     * @param status       HTTP 상태 코드
     * @param code         도메인 에러 코드 (예: "INV-001")
     * @param userMessage  사용자에게 노출하는 메시지
     * @param debugMessage 내부 로깅용 상세 메시지
     */
    record MappedError(HttpStatus status, String code, String userMessage, String debugMessage) {}
}
