package com.ryuqq.otatoy.domain.common.sort;

/**
 * 정렬 키를 나타내는 인터페이스. 각 도메인별 정렬 기준 enum이 구현한다.
 */
public interface SortKey {
    String fieldName();
}
