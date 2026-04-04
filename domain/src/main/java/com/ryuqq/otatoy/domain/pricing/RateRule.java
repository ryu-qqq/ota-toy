package com.ryuqq.otatoy.domain.pricing;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class RateRule {

    private final RateRuleId id;
    private final RatePlanId ratePlanId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal basePrice;
    private BigDecimal weekdayPrice;
    private BigDecimal fridayPrice;
    private BigDecimal saturdayPrice;
    private BigDecimal sundayPrice;
    private final Instant createdAt;
    private Instant updatedAt;

    private RateRule(RateRuleId id, RatePlanId ratePlanId, LocalDate startDate, LocalDate endDate,
                     BigDecimal basePrice, BigDecimal weekdayPrice, BigDecimal fridayPrice,
                     BigDecimal saturdayPrice, BigDecimal sundayPrice,
                     Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.ratePlanId = ratePlanId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.basePrice = basePrice;
        this.weekdayPrice = weekdayPrice;
        this.fridayPrice = fridayPrice;
        this.saturdayPrice = saturdayPrice;
        this.sundayPrice = sundayPrice;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static RateRule forNew(RatePlanId ratePlanId, LocalDate startDate, LocalDate endDate,
                                   BigDecimal basePrice, BigDecimal weekdayPrice,
                                   BigDecimal fridayPrice, BigDecimal saturdayPrice,
                                   BigDecimal sundayPrice, Instant now) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("시작일과 종료일은 필수입니다");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("종료일은 시작일 이후여야 합니다");
        }
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("기본 가격은 0 이상이어야 합니다");
        }
        return new RateRule(RateRuleId.of(null), ratePlanId, startDate, endDate,
                basePrice, weekdayPrice, fridayPrice, saturdayPrice, sundayPrice,
                now, now);
    }

    public static RateRule reconstitute(RateRuleId id, RatePlanId ratePlanId, LocalDate startDate, LocalDate endDate,
                                         BigDecimal basePrice, BigDecimal weekdayPrice,
                                         BigDecimal fridayPrice, BigDecimal saturdayPrice,
                                         BigDecimal sundayPrice, Instant createdAt, Instant updatedAt) {
        return new RateRule(id, ratePlanId, startDate, endDate,
                basePrice, weekdayPrice, fridayPrice, saturdayPrice, sundayPrice,
                createdAt, updatedAt);
    }

    public BigDecimal calculatePrice(LocalDate date) {
        if (date.isBefore(startDate) || date.isAfter(endDate)) {
            throw new IllegalArgumentException("해당 날짜는 이 요금 규칙 범위 밖입니다: " + date);
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();

        return switch (dayOfWeek) {
            case FRIDAY -> fridayPrice != null ? fridayPrice : basePrice;
            case SATURDAY -> saturdayPrice != null ? saturdayPrice : basePrice;
            case SUNDAY -> sundayPrice != null ? sundayPrice : basePrice;
            default -> weekdayPrice != null ? weekdayPrice : basePrice;
        };
    }

    /**
     * 해당 날짜의 최종 가격을 결정한다.
     * RateOverride가 존재하면 오버라이드 가격을, 없으면 요일 기반 기본가를 반환한다.
     * (캐싱 설계: RateRule 기본가 -> RateOverride 덮어쓰기)
     */
    public BigDecimal resolvePrice(LocalDate date, List<RateOverride> overrides) {
        if (!covers(date)) {
            throw new IllegalArgumentException("해당 날짜는 이 요금 규칙 범위 밖입니다: " + date);
        }

        if (overrides != null) {
            for (RateOverride override : overrides) {
                if (date.equals(override.overrideDate())) {
                    return override.price();
                }
            }
        }

        return calculatePrice(date);
    }

    public boolean covers(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public RateRuleId id() { return id; }
    public RatePlanId ratePlanId() { return ratePlanId; }
    public LocalDate startDate() { return startDate; }
    public LocalDate endDate() { return endDate; }
    public BigDecimal basePrice() { return basePrice; }
    public BigDecimal weekdayPrice() { return weekdayPrice; }
    public BigDecimal fridayPrice() { return fridayPrice; }
    public BigDecimal saturdayPrice() { return saturdayPrice; }
    public BigDecimal sundayPrice() { return sundayPrice; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RateRule r)) return false;
        return id != null && id.equals(r.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
