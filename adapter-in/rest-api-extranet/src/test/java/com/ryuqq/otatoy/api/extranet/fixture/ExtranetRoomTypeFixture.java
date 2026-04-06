package com.ryuqq.otatoy.api.extranet.fixture;

/**
 * Extranet RoomType API 테스트용 Fixture.
 * 요청 JSON을 중앙에서 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public final class ExtranetRoomTypeFixture {

    private ExtranetRoomTypeFixture() {}

    /**
     * 객실 유형 등록 전체 필드 요청 JSON
     */
    public static String registerRoomTypeRequest() {
        return """
            {
                "name": "디럭스 더블",
                "description": "넓은 객실",
                "areaSqm": 33.0,
                "areaPyeong": "10평",
                "baseOccupancy": 2,
                "maxOccupancy": 4,
                "baseInventory": 5,
                "checkInTime": "15:00",
                "checkOutTime": "11:00",
                "beds": [
                    { "bedTypeId": 1, "quantity": 1 },
                    { "bedTypeId": 2, "quantity": 2 }
                ],
                "views": [
                    { "viewTypeId": 1 }
                ]
            }
            """;
    }

    /**
     * 객실 유형 등록 최소 필수 필드만 포함한 요청 JSON
     */
    public static String registerRoomTypeMinimalRequest() {
        return """
            {
                "name": "스탠다드",
                "areaSqm": 20.0,
                "baseOccupancy": 2,
                "maxOccupancy": 2,
                "baseInventory": 3,
                "checkInTime": "15:00",
                "checkOutTime": "11:00"
            }
            """;
    }

    /**
     * 객실 유형 등록 시 필수 필드 누락 요청 JSON (name 누락)
     */
    public static String registerRoomTypeInvalidRequest() {
        return """
            {
                "areaSqm": 20.0,
                "baseOccupancy": 2,
                "maxOccupancy": 2,
                "baseInventory": 3,
                "checkInTime": "15:00",
                "checkOutTime": "11:00"
            }
            """;
    }

    /**
     * 객실 유형 등록 시 baseOccupancy = 0 요청 JSON (@Positive 위반)
     */
    public static String registerRoomTypeBaseOccupancyZeroRequest() {
        return """
            {
                "name": "스탠다드",
                "areaSqm": 20.0,
                "baseOccupancy": 0,
                "maxOccupancy": 2,
                "baseInventory": 3,
                "checkInTime": "15:00",
                "checkOutTime": "11:00"
            }
            """;
    }

    /**
     * 객실 유형 등록 시 maxOccupancy < baseOccupancy 요청 JSON (도메인 예외)
     */
    public static String registerRoomTypeMaxLessThanBaseRequest() {
        return """
            {
                "name": "스탠다드",
                "areaSqm": 20.0,
                "baseOccupancy": 4,
                "maxOccupancy": 2,
                "baseInventory": 3,
                "checkInTime": "15:00",
                "checkOutTime": "11:00"
            }
            """;
    }

    /**
     * 객실 유형 등록 시 areaSqm 음수 요청 JSON (@PositiveOrZero 위반)
     */
    public static String registerRoomTypeNegativeAreaRequest() {
        return """
            {
                "name": "스탠다드",
                "areaSqm": -5.0,
                "baseOccupancy": 2,
                "maxOccupancy": 2,
                "baseInventory": 3,
                "checkInTime": "15:00",
                "checkOutTime": "11:00"
            }
            """;
    }

    /**
     * 객실 유형 등록 시 beds 내 quantity = 0 요청 JSON (@Positive 위반)
     */
    public static String registerRoomTypeBedQuantityZeroRequest() {
        return """
            {
                "name": "스탠다드",
                "areaSqm": 20.0,
                "baseOccupancy": 2,
                "maxOccupancy": 2,
                "baseInventory": 3,
                "checkInTime": "15:00",
                "checkOutTime": "11:00",
                "beds": [
                    { "bedTypeId": 1, "quantity": 0 }
                ]
            }
            """;
    }

    /**
     * 객실 유형 등록 시 checkInTime 유효하지 않은 포맷 요청 JSON
     */
    public static String registerRoomTypeInvalidCheckInTimeRequest() {
        return """
            {
                "name": "스탠다드",
                "areaSqm": 20.0,
                "baseOccupancy": 2,
                "maxOccupancy": 2,
                "baseInventory": 3,
                "checkInTime": "25:00",
                "checkOutTime": "11:00"
            }
            """;
    }
}
