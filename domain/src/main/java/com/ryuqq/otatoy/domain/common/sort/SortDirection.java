package com.ryuqq.otatoy.domain.common.sort;

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
