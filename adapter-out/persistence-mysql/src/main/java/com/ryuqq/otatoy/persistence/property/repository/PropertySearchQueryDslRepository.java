package com.ryuqq.otatoy.persistence.property.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.otatoy.domain.accommodation.AmenityType;
import com.ryuqq.otatoy.domain.common.sort.SortDirection;
import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.property.PropertySortKey;
import com.ryuqq.otatoy.persistence.inventory.entity.QInventoryJpaEntity;
import com.ryuqq.otatoy.persistence.pricing.entity.QRateJpaEntity;
import com.ryuqq.otatoy.persistence.pricing.entity.QRatePlanJpaEntity;
import com.ryuqq.otatoy.persistence.property.entity.QPropertyAmenityJpaEntity;
import com.ryuqq.otatoy.persistence.property.entity.QPropertyJpaEntity;
import com.ryuqq.otatoy.persistence.roomtype.entity.QRoomTypeJpaEntity;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 고객 숙소 검색 전용 QueryDsl Repository.
 * Property, RoomType, Inventory, Rate, RatePlan, PropertyAmenity를 크로스 BC 조인하여
 * 검색 조건에 맞는 Property ID + 최저가를 조회한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Repository
public class PropertySearchQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    private static final QPropertyJpaEntity property = QPropertyJpaEntity.propertyJpaEntity;
    private static final QRoomTypeJpaEntity roomType = QRoomTypeJpaEntity.roomTypeJpaEntity;
    private static final QInventoryJpaEntity inventory = QInventoryJpaEntity.inventoryJpaEntity;
    private static final QRateJpaEntity rate = QRateJpaEntity.rateJpaEntity;
    private static final QRatePlanJpaEntity ratePlan = QRatePlanJpaEntity.ratePlanJpaEntity;
    private static final QPropertyAmenityJpaEntity amenity = QPropertyAmenityJpaEntity.propertyAmenityJpaEntity;

    public PropertySearchQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 검색 조건에 맞는 Property ID 목록을 커서 기반으로 조회한다.
     * 숙소 검색의 핵심 쿼리로, 여러 테이블을 조인한다.
     *
     * @return 조건을 만족하는 Property ID 목록 (size + 1개, hasNext 판단용)
     */
    public List<Long> searchPropertyIds(String keyword, String region, Long propertyTypeId,
                                         LocalDate checkIn, LocalDate checkOut,
                                         int guests, Money minPrice, Money maxPrice,
                                         List<AmenityType> amenityTypes, boolean freeCancellationOnly,
                                         Integer starRating,
                                         PropertySortKey sortKey, SortDirection direction,
                                         int size, Long cursor) {

        // 기간 내 모든 날짜에 재고가 있는 객실이 있는 숙소를 찾는다.
        // 서브쿼리로 조건을 만족하는 property ID를 추출한다.
        long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);

        BooleanBuilder builder = new BooleanBuilder();

        // 기본 조건: ACTIVE + soft delete
        builder.and(property.status.eq("ACTIVE"));
        builder.and(property.deleted.isFalse());

        // 키워드 검색 (숙소명, 주소, 지역)
        if (keyword != null && !keyword.isBlank()) {
            builder.and(
                    property.name.containsIgnoreCase(keyword)
                            .or(property.address.containsIgnoreCase(keyword))
                            .or(property.region.containsIgnoreCase(keyword))
            );
        }

        // 지역 필터
        if (region != null && !region.isBlank()) {
            builder.and(property.region.eq(region));
        }

        // 숙소 유형 필터
        if (propertyTypeId != null) {
            builder.and(property.propertyTypeId.eq(propertyTypeId));
        }

        // 커서 조건
        if (cursor != null) {
            builder.and(property.id.gt(cursor));
        }

        // 객실 조건: 인원 수용 가능 + ACTIVE 상태의 객실이 있는 숙소
        builder.and(JPAExpressions
                .selectOne()
                .from(roomType)
                .where(
                        roomType.propertyId.eq(property.id),
                        roomType.maxOccupancy.goe(guests),
                        roomType.status.eq("ACTIVE"),
                        roomType.deleted.isFalse()
                )
                .exists());

        // 재고 조건: 기간 내 모든 날짜에 가용 재고가 있는 객실이 존재
        builder.and(JPAExpressions
                .select(inventory.roomTypeId)
                .from(inventory)
                .innerJoin(roomType).on(
                        inventory.roomTypeId.eq(roomType.id),
                        roomType.propertyId.eq(property.id),
                        roomType.maxOccupancy.goe(guests),
                        roomType.status.eq("ACTIVE"),
                        roomType.deleted.isFalse()
                )
                .where(
                        inventory.inventoryDate.goe(checkIn),
                        inventory.inventoryDate.lt(checkOut),
                        inventory.availableCount.gt(0),
                        inventory.stopSell.isFalse(),
                        inventory.deleted.isFalse()
                )
                .groupBy(inventory.roomTypeId)
                .having(inventory.roomTypeId.count().eq(nights))
                .exists());

        // 무료취소 필터
        if (freeCancellationOnly) {
            builder.and(JPAExpressions
                    .selectOne()
                    .from(ratePlan)
                    .innerJoin(roomType).on(
                            ratePlan.roomTypeId.eq(roomType.id),
                            roomType.propertyId.eq(property.id),
                            roomType.deleted.isFalse()
                    )
                    .where(
                            ratePlan.freeCancellation.isTrue(),
                            ratePlan.deleted.isFalse()
                    )
                    .exists());
        }

        // 편의시설 필터: 모든 편의시설 유형이 존재해야 함 (AND 조건)
        if (amenityTypes != null && !amenityTypes.isEmpty()) {
            for (AmenityType amenityType : amenityTypes) {
                builder.and(JPAExpressions
                        .selectOne()
                        .from(amenity)
                        .where(
                                amenity.propertyId.eq(property.id),
                                amenity.amenityType.eq(amenityType.name()),
                                amenity.deleted.isFalse()
                        )
                        .exists());
            }
        }

        // 가격 필터: 기간 내 1박 평균 가격 기준
        if (minPrice != null || maxPrice != null) {
            // Rate 서브쿼리로 가격 필터링
            NumberExpression<Double> avgPrice = rate.basePrice.avg().doubleValue();

            BooleanBuilder priceBuilder = new BooleanBuilder();
            priceBuilder.and(rate.rateDate.goe(checkIn));
            priceBuilder.and(rate.rateDate.lt(checkOut));
            priceBuilder.and(rate.deleted.isFalse());
            priceBuilder.and(ratePlan.deleted.isFalse());
            priceBuilder.and(roomType.propertyId.eq(property.id));
            priceBuilder.and(roomType.deleted.isFalse());
            priceBuilder.and(ratePlan.roomTypeId.eq(roomType.id));
            priceBuilder.and(rate.ratePlanId.eq(ratePlan.id));

            BooleanBuilder havingBuilder = new BooleanBuilder();
            if (minPrice != null) {
                havingBuilder.and(avgPrice.goe(minPrice.amount().doubleValue()));
            }
            if (maxPrice != null) {
                havingBuilder.and(avgPrice.loe(maxPrice.amount().doubleValue()));
            }

            builder.and(JPAExpressions
                    .select(rate.ratePlanId)
                    .from(rate)
                    .innerJoin(ratePlan).on(rate.ratePlanId.eq(ratePlan.id))
                    .innerJoin(roomType).on(ratePlan.roomTypeId.eq(roomType.id))
                    .where(priceBuilder)
                    .groupBy(rate.ratePlanId)
                    .having(havingBuilder)
                    .exists());
        }

        return queryFactory
                .select(property.id)
                .from(property)
                .where(builder)
                .orderBy(property.id.asc())
                .limit(size + 1)
                .fetch();
    }
}
