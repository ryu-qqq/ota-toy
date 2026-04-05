package com.ryuqq.otatoy.persistence.property.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.otatoy.persistence.property.entity.PropertyAttributeValueJpaEntity;
import com.ryuqq.otatoy.persistence.property.entity.QPropertyAttributeValueJpaEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PropertyAttributeValue 조회 전용 QueryDsl Repository.
 * 모든 조회에 deleted.isFalse() soft delete 필터를 포함한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Repository
public class PropertyAttributeValueQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private static final QPropertyAttributeValueJpaEntity attributeValue = QPropertyAttributeValueJpaEntity.propertyAttributeValueJpaEntity;

    public PropertyAttributeValueQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 해당 숙소의 활성(삭제되지 않은) 속성값을 조회한다.
     */
    public List<PropertyAttributeValueJpaEntity> findByPropertyId(Long propertyId) {
        return queryFactory
                .selectFrom(attributeValue)
                .where(
                        attributeValue.propertyId.eq(propertyId),
                        attributeValue.deleted.isFalse()
                )
                .fetch();
    }
}
