package com.ryuqq.otatoy.persistence.property.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.otatoy.persistence.property.entity.PropertyJpaEntity;
import com.ryuqq.otatoy.persistence.property.entity.QPropertyJpaEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Property 조회 전용 QueryDsl Repository.
 * 모든 조회에 deleted.isFalse() soft delete 필터를 포함한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Repository
public class PropertyQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private static final QPropertyJpaEntity property = QPropertyJpaEntity.propertyJpaEntity;

    public PropertyQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public Optional<PropertyJpaEntity> findById(Long id) {
        PropertyJpaEntity result = queryFactory
                .selectFrom(property)
                .where(
                        property.id.eq(id),
                        property.deleted.isFalse()
                )
                .fetchOne();
        return Optional.ofNullable(result);
    }

    public boolean existsById(Long id) {
        Integer result = queryFactory
                .selectOne()
                .from(property)
                .where(
                        property.id.eq(id),
                        property.deleted.isFalse()
                )
                .fetchFirst();
        return result != null;
    }
}
