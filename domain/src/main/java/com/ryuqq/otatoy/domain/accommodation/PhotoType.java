package com.ryuqq.otatoy.domain.accommodation;

public enum PhotoType {

    EXTERIOR("외관"),
    LOBBY("로비"),
    ROOM("객실"),
    BATHROOM("욕실"),
    VIEW("전망"),
    FACILITY("시설"),
    RESTAURANT("레스토랑"),
    POOL("수영장"),
    OTHER("기타");

    private final String displayName;

    PhotoType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
