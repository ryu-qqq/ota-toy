package com.ryuqq.otatoy.application.property.dto.query;

import com.ryuqq.otatoy.domain.partner.PartnerId;

/**
 * 파트너 숙소 목록 조회 UseCase 입력 DTO.
 * Controller(ApiMapper)에서 변환하여 전달한다 (APP-DTO-001).
 * 인스턴스 메서드 금지 — 순수 데이터 컨테이너 역할만 한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record ExtranetSearchPropertyQuery(
        PartnerId partnerId,
        int size,
        Long cursor
) {
}
