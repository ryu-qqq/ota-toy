package com.ryuqq.otatoy.persistence.propertytype.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.otatoy.persistence.propertytype.entity.PropertyTypeAttributeJpaEntity;
import com.ryuqq.otatoy.persistence.propertytype.entity.PropertyTypeJpaEntity;
import com.ryuqq.otatoy.persistence.propertytype.entity.QPropertyTypeAttributeJpaEntity;
import com.ryuqq.otatoy.persistence.propertytype.entity.QPropertyTypeJpaEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * PropertyType 조회 전용 QueryDsl Repository.
 * 모든 조회에 deleted.isFalse() soft delete 필터를 포함한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Repository
public class PropertyTypeQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private static final QPropertyTypeJpaEntity propertyType = QPropertyTypeJpaEntity.propertyTypeJpaEntity;
    private static final QPropertyTypeAttributeJpaEntity propertyTypeAttribute = QPropertyTypeAttributeJpaEntity.propertyTypeAttributeJpaEntity;

    public PropertyTypeQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public Optional<PropertyTypeJpaEntity> findById(Long id) {
        PropertyTypeJpaEntity result = queryFactory
                .selectFrom(propertyType)
                .where(
                        propertyType.id.eq(id),
                        propertyType.deleted.isFalse()
                )
                .fetchOne();
        return Optional.ofNullable(result);
    }

    public boolean existsById(Long id) {
        Integer result = queryFactory
                .selectOne()
                .from(propertyType)
                .where(
                        propertyType.id.eq(id),
                        propertyType.deleted.isFalse()
                )
                .fetchFirst();
        return result != null;
    }

    public List<PropertyTypeAttributeJpaEntity> findAttributesByPropertyTypeId(Long propertyTypeId) {
        return queryFactory
                .selectFrom(propertyTypeAttribute)
                .where(
                        propertyTypeAttribute.propertyTypeId.eq(propertyTypeId),
                        propertyTypeAttribute.deleted.isFalse()
                )
                .orderBy(propertyTypeAttribute.sortOrder.asc())
                .fetch();
    }
}
