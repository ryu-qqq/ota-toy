package com.ryuqq.otatoy.domain.common;

/**
 * 에러의 성격을 나타내는 카테고리.
 * HTTP 상태와는 무관한 순수 도메인 개념이다.
 * Adapter 레이어에서 이 카테고리를 HTTP 상태로 매핑한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public enum ErrorCategory {

    /** 요청한 리소스를 찾을 수 없음 */
    NOT_FOUND("리소스 없음"),

    /** 입력값 또는 비즈니스 규칙 위반 */
    VALIDATION("검증 실패"),

    /** 상태 충돌 (이미 존재, 중복, 소진) */
    CONFLICT("상태 충돌"),

    /** 금지된 행위 (중지, 불가) */
    FORBIDDEN("금지된 행위");

    private final String displayName;

    ErrorCategory(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
