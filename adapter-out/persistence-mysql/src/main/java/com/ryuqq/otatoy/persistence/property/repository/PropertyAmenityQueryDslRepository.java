package com.ryuqq.otatoy.persistence.property.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.otatoy.persistence.property.entity.PropertyAmenityJpaEntity;
import com.ryuqq.otatoy.persistence.property.entity.QPropertyAmenityJpaEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PropertyAmenity 조회 전용 QueryDsl Repository.
 * 모든 조회에 deleted.isFalse() soft delete 필터를 포함한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Repository
public class PropertyAmenityQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private static final QPropertyAmenityJpaEntity propertyAmenity = QPropertyAmenityJpaEntity.propertyAmenityJpaEntity;

    public PropertyAmenityQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 해당 숙소의 활성(삭제되지 않은) 편의시설을 조회한다.
     */
    public List<PropertyAmenityJpaEntity> findByPropertyId(Long propertyId) {
        return queryFactory
                .selectFrom(propertyAmenity)
                .where(
                        propertyAmenity.propertyId.eq(propertyId),
                        propertyAmenity.deleted.isFalse()
                )
                .orderBy(propertyAmenity.sortOrder.asc())
                .fetch();
    }
}
