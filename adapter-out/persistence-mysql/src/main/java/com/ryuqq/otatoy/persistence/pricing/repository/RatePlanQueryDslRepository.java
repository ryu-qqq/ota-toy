package com.ryuqq.otatoy.persistence.pricing.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.otatoy.persistence.pricing.entity.QRatePlanJpaEntity;
import com.ryuqq.otatoy.persistence.pricing.entity.RatePlanJpaEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * RatePlan 조회 전용 QueryDsl Repository.
 * 모든 조회에 deleted.isFalse() soft delete 필터를 포함한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Repository
public class RatePlanQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private static final QRatePlanJpaEntity ratePlan = QRatePlanJpaEntity.ratePlanJpaEntity;

    public RatePlanQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public Optional<RatePlanJpaEntity> findById(Long id) {
        RatePlanJpaEntity result = queryFactory
                .selectFrom(ratePlan)
                .where(
                        ratePlan.id.eq(id),
                        ratePlan.deleted.isFalse()
                )
                .fetchOne();
        return Optional.ofNullable(result);
    }

    public boolean existsById(Long id) {
        Integer result = queryFactory
                .selectOne()
                .from(ratePlan)
                .where(
                        ratePlan.id.eq(id),
                        ratePlan.deleted.isFalse()
                )
                .fetchFirst();
        return result != null;
    }

    public List<RatePlanJpaEntity> findByRoomTypeIds(List<Long> roomTypeIds) {
        return queryFactory
                .selectFrom(ratePlan)
                .where(
                        ratePlan.roomTypeId.in(roomTypeIds),
                        ratePlan.deleted.isFalse()
                )
                .fetch();
    }
}
