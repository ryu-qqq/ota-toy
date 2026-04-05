package com.ryuqq.otatoy.persistence.property.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.otatoy.persistence.property.entity.PropertyJpaEntity;
import com.ryuqq.otatoy.persistence.property.entity.QPropertyJpaEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    /**
     * 특정 파트너의 숙소 목록을 커서 기반으로 조회한다.
     * size + 1 개를 조회하여 다음 페이지 존재 여부를 판단한다.
     */
    public List<PropertyJpaEntity> findByPartnerId(Long partnerId, int size, Long cursor) {
        return queryFactory
                .selectFrom(property)
                .where(
                        property.partnerId.eq(partnerId),
                        property.deleted.isFalse(),
                        cursorCondition(cursor)
                )
                .orderBy(property.id.asc())
                .limit(size + 1)
                .fetch();
    }

    private BooleanExpression cursorCondition(Long cursor) {
        if (cursor == null) {
            return null;
        }
        return property.id.gt(cursor);
    }
}
