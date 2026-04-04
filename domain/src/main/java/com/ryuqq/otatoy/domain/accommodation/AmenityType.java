package com.ryuqq.otatoy.domain.accommodation;

public enum AmenityType {

    // 숙소 편의시설
    PARKING("주차장"),
    POOL("수영장"),
    FITNESS("피트니스"),
    RESTAURANT("레스토랑"),
    FRONT_DESK_24H("24시간 프런트"),
    ROOM_SERVICE("룸서비스"),

    // 객실 편의시설
    AIR_CONDITIONING("에어컨"),
    TV("TV"),
    WIFI("와이파이"),
    PRIVATE_BATHROOM("전용 욕실"),
    BALCONY("발코니"),
    KITCHEN("주방"),
    REFRIGERATOR("냉장고"),
    ELECTRIC_KETTLE("전기 주전자"),
    HAIR_DRYER("헤어드라이어"),
    BATHTUB("욕조"),
    SHOWER("샤워실"),
    TOWELS("타월"),
    TOILETRIES("세면도구"),
    DESK("책상"),
    SOFA("소파"),
    MINI_BAR("미니바"),
    SAFE_BOX("객실 내 안전 금고"),
    SLIPPERS("슬리퍼"),
    FREE_WATER("무료 생수"),
    NON_SMOKING("금연"),
    HEATING("난방"),

    // 기타
    OTHER("기타");

    private final String displayName;

    AmenityType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
