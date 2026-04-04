package com.ryuqq.otatoy.domain.common.query;

/**
 * 오프셋 기반 페이지네이션 메타 정보.
 */
public record PageMeta(int page, int size, long totalElements, int totalPages) {
}
