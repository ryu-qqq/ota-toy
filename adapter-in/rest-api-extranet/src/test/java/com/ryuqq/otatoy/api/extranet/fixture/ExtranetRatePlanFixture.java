package com.ryuqq.otatoy.api.extranet.fixture;

/**
 * Extranet RatePlan API 테스트용 Fixture.
 * 요청 JSON을 중앙에서 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public final class ExtranetRatePlanFixture {

    private ExtranetRatePlanFixture() {}

    /**
     * 요금 정책 등록 전체 필드 요청 JSON
     */
    public static String registerRatePlanRequest() {
        return """
            {
                "name": "기본 요금",
                "freeCancellation": true,
                "nonRefundable": false,
                "freeCancellationDeadlineDays": 3,
                "cancellationPolicyText": "체크인 3일 전까지 무료 취소 가능",
                "paymentPolicy": "PREPAY"
            }
            """;
    }

    /**
     * 요금 정책 등록 시 필수 필드 누락 요청 JSON (name 누락)
     */
    public static String registerRatePlanInvalidRequest() {
        return """
            {
                "freeCancellation": true,
                "paymentPolicy": "PREPAY"
            }
            """;
    }

    /**
     * 요금/재고 설정 요청 JSON
     */
    public static String setRateAndInventoryRequest() {
        return """
            {
                "startDate": "2026-05-01",
                "endDate": "2026-05-31",
                "basePrice": 100000,
                "weekdayPrice": 90000,
                "fridayPrice": 110000,
                "saturdayPrice": 130000,
                "sundayPrice": 95000,
                "baseInventory": 10,
                "overrides": [
                    {
                        "date": "2026-05-05",
                        "price": 150000,
                        "reason": "어린이날 특가"
                    }
                ]
            }
            """;
    }

    /**
     * 요금/재고 설정 시 필수 필드 누락 요청 JSON (startDate 누락)
     */
    public static String setRateAndInventoryInvalidRequest() {
        return """
            {
                "endDate": "2026-05-31",
                "basePrice": 100000,
                "baseInventory": 10
            }
            """;
    }

    /**
     * 요금/재고 설정 시 baseInventory 음수 요청 JSON (@Min(0) 위반)
     */
    public static String setRateAndInventoryNegativeInventoryRequest() {
        return """
            {
                "startDate": "2026-05-01",
                "endDate": "2026-05-31",
                "basePrice": 100000,
                "baseInventory": -1
            }
            """;
    }

    /**
     * 요금/재고 설정 시 overrides 내부 price = null 요청 JSON (@NotNull 위반)
     */
    public static String setRateAndInventoryOverridePriceNullRequest() {
        return """
            {
                "startDate": "2026-05-01",
                "endDate": "2026-05-31",
                "basePrice": 100000,
                "baseInventory": 10,
                "overrides": [
                    {
                        "date": "2026-05-05",
                        "price": null,
                        "reason": "테스트"
                    }
                ]
            }
            """;
    }
}
