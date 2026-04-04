package com.ryuqq.otatoy.domain.pricing;

import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.domain.supplier.SupplierId;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * pricing BC 테스트용 Fixture.
 * 각 메서드는 특정 상태의 도메인 객체를 반환한다.
 */
public final class PricingFixtures {

    private PricingFixtures() {}

    // 공통 상수
    public static final Instant NOW = Instant.parse("2026-04-04T00:00:00Z");
    public static final Instant LATER = Instant.parse("2026-04-05T00:00:00Z");
    public static final RoomTypeId ROOM_TYPE_ID = RoomTypeId.of(1L);
    public static final SupplierId SUPPLIER_ID = SupplierId.of(10L);
    public static final RatePlanName DEFAULT_NAME = RatePlanName.of("기본 요금제");
    public static final RatePlanId RATE_PLAN_ID = RatePlanId.of(1L);
    public static final RateRuleId RATE_RULE_ID = RateRuleId.of(1L);

    // --- RatePlan ---

    public static final CancellationPolicy FREE_CANCELLATION = CancellationPolicy.of(true, false, 3, "체크인 3일 전까지 무료 취소");
    public static final CancellationPolicy NON_REFUNDABLE = CancellationPolicy.of(false, true, 0, "환불 불가");

    /** DIRECT 소스의 기본 RatePlan (무료 취소, 선결제) */
    public static RatePlan directRatePlan() {
        return RatePlan.forNew(
                ROOM_TYPE_ID, DEFAULT_NAME, SourceType.DIRECT, null,
                FREE_CANCELLATION, PaymentPolicy.PREPAY, NOW
        );
    }

    /** SUPPLIER 소스의 기본 RatePlan (환불 불가) */
    public static RatePlan supplierRatePlan() {
        return RatePlan.forNew(
                ROOM_TYPE_ID, DEFAULT_NAME, SourceType.SUPPLIER, SUPPLIER_ID,
                NON_REFUNDABLE, PaymentPolicy.PREPAY, NOW
        );
    }

    /** DB 복원된 RatePlan (ID 있음) */
    public static RatePlan reconstitutedRatePlan(long id) {
        return RatePlan.reconstitute(
                RatePlanId.of(id), ROOM_TYPE_ID, DEFAULT_NAME,
                SourceType.DIRECT, null, FREE_CANCELLATION,
                PaymentPolicy.PREPAY, NOW, NOW
        );
    }

    // --- RateRule ---

    /** 4월 한 달 기간의 기본 요금 규칙 (평일 10만, 금 12만, 토 15만, 일 11만) */
    public static RateRule defaultRateRule() {
        return RateRule.forNew(
                RATE_PLAN_ID,
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30),
                BigDecimal.valueOf(100_000), BigDecimal.valueOf(100_000),
                BigDecimal.valueOf(120_000), BigDecimal.valueOf(150_000),
                BigDecimal.valueOf(110_000), NOW
        );
    }

    /** 요일별 가격이 모두 null인 요금 규칙 (basePrice만 존재) */
    public static RateRule basePriceOnlyRateRule() {
        return RateRule.forNew(
                RATE_PLAN_ID,
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30),
                BigDecimal.valueOf(80_000), null, null, null, null, NOW
        );
    }

    /** DB 복원된 RateRule (ID 있음) */
    public static RateRule reconstitutedRateRule(long id) {
        return RateRule.reconstitute(
                RateRuleId.of(id), RATE_PLAN_ID,
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30),
                BigDecimal.valueOf(100_000), BigDecimal.valueOf(100_000),
                BigDecimal.valueOf(120_000), BigDecimal.valueOf(150_000),
                BigDecimal.valueOf(110_000), NOW, NOW
        );
    }

    // --- RateOverride ---

    // RateRule 기본 날짜 범위 (defaultRateRule과 동일)
    public static final LocalDate RULE_START_DATE = LocalDate.of(2026, 4, 1);
    public static final LocalDate RULE_END_DATE = LocalDate.of(2026, 4, 30);

    /** 4/5(토) 공휴일 오버라이드 (17만원) */
    public static RateOverride defaultOverride() {
        return RateOverride.forNew(
                RATE_RULE_ID, RULE_START_DATE, RULE_END_DATE,
                LocalDate.of(2026, 4, 5),
                BigDecimal.valueOf(170_000), "공휴일 특가", NOW
        );
    }

    /** DB 복원된 RateOverride */
    public static RateOverride reconstitutedOverride(long id) {
        return RateOverride.reconstitute(
                RateOverrideId.of(id), RATE_RULE_ID,
                LocalDate.of(2026, 4, 5), BigDecimal.valueOf(170_000),
                "공휴일 특가", NOW
        );
    }

    // --- Rate ---

    /** 특정 날짜의 Rate 스냅샷 */
    public static Rate defaultRate(LocalDate date, BigDecimal price) {
        return Rate.forNew(RATE_PLAN_ID, date, price, NOW);
    }

    /** DB 복원된 Rate */
    public static Rate reconstitutedRate(long id, LocalDate date, BigDecimal price) {
        return Rate.reconstitute(
                RateId.of(id), RATE_PLAN_ID, date, price, NOW, NOW
        );
    }

    // --- RatePlanAddOn ---

    /** 유료 조식 Add-on (23,000원) */
    public static RatePlanAddOn breakfastAddOn() {
        return RatePlanAddOn.forNew(
                RATE_PLAN_ID, AddOnType.of("BREAKFAST"),
                AddOnName.of("조식 뷔페"), BigDecimal.valueOf(23_000),
                false, NOW
        );
    }

    /** 무료 포함 Add-on (수건/어메니티) */
    public static RatePlanAddOn freeIncludedAddOn() {
        return RatePlanAddOn.forNew(
                RATE_PLAN_ID, AddOnType.of("AMENITY"),
                AddOnName.of("수건 세트"), BigDecimal.ZERO,
                true, NOW
        );
    }

    /** DB 복원된 RatePlanAddOn */
    public static RatePlanAddOn reconstitutedAddOn(long id) {
        return RatePlanAddOn.reconstitute(
                RatePlanAddOnId.of(id), RATE_PLAN_ID,
                AddOnType.of("BREAKFAST"), AddOnName.of("조식 뷔페"),
                BigDecimal.valueOf(23_000), false, NOW
        );
    }

    // --- Override 리스트 ---

    /** resolvePrice 테스트용: 4/5 오버라이드가 포함된 리스트 */
    public static List<RateOverride> overrideListWithApril5() {
        return List.of(
                RateOverride.forNew(RATE_RULE_ID, RULE_START_DATE, RULE_END_DATE,
                        LocalDate.of(2026, 4, 5),
                        BigDecimal.valueOf(170_000), "공휴일", NOW),
                RateOverride.forNew(RATE_RULE_ID, RULE_START_DATE, RULE_END_DATE,
                        LocalDate.of(2026, 4, 15),
                        BigDecimal.valueOf(90_000), "비수기 할인", NOW)
        );
    }
}
