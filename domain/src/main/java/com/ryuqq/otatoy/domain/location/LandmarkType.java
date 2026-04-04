package com.ryuqq.otatoy.domain.location;

public enum LandmarkType {

    STATION("역"),
    TOURIST("관광지"),
    AIRPORT("공항"),
    SHOPPING("쇼핑"),
    PARK("공원");

    private final String displayName;

    LandmarkType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
