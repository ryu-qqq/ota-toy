package com.ryuqq.otatoy.domain.property;

import com.ryuqq.otatoy.domain.partner.PartnerId;

/**
 * Extranet 숙소 목록 조회 조건.
 * 파트너가 자기 숙소를 조회할 때 사용한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record ExtranetPropertySliceCriteria(
    PartnerId partnerId,
    int size,
    Long cursor
) {

    public ExtranetPropertySliceCriteria {
        if (partnerId == null) {
            throw new IllegalArgumentException("파트너 ID는 필수입니다");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("페이지 크기는 1~100이어야 합니다");
        }
    }
}
