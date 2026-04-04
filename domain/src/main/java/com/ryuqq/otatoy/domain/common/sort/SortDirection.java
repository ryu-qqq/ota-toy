package com.ryuqq.otatoy.domain.common.sort;

/**
 * 정렬 방향.
 * ASC: 오름차순, DESC: 내림차순.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public enum SortDirection {

    ASC("오름차순"),
    DESC("내림차순");

    private final String displayName;

    SortDirection(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
