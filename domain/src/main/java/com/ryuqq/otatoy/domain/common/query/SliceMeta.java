package com.ryuqq.otatoy.domain.common.query;

/**
 * 커서 기반 페이지네이션의 슬라이스 메타 정보.
 */
public record SliceMeta(boolean hasNext, Long nextCursor) {
}
