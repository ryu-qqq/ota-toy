package com.ryuqq.otatoy.domain.pricing;

public enum SourceType {

    DIRECT("직접 입점"),
    SUPPLIER("외부 공급자");

    private final String displayName;

    SourceType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
