package com.ryuqq.otatoy.persistence.pricing.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.otatoy.persistence.pricing.entity.QRateJpaEntity;
import com.ryuqq.otatoy.persistence.pricing.entity.RateJpaEntity;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Rate 조회 전용 QueryDsl Repository.
 * 모든 조회에 deleted.isFalse() soft delete 필터를 포함한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Repository
public class RateQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private static final QRateJpaEntity rate = QRateJpaEntity.rateJpaEntity;

    public RateQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 특정 요금 정책들의 날짜 범위에 해당하는 요금 목록을 조회한다.
     * startDate 포함, endDate 미포함.
     */
    public List<RateJpaEntity> findByRatePlanIdsAndDateRange(List<Long> ratePlanIds,
                                                              LocalDate startDate,
                                                              LocalDate endDate) {
        return queryFactory
                .selectFrom(rate)
                .where(
                        rate.ratePlanId.in(ratePlanIds),
                        rate.rateDate.goe(startDate),
                        rate.rateDate.lt(endDate),
                        rate.deleted.isFalse()
                )
                .fetch();
    }
}
