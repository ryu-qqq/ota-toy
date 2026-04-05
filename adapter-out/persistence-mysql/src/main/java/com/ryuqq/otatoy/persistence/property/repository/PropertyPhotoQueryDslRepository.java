package com.ryuqq.otatoy.persistence.property.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.otatoy.persistence.property.entity.PropertyPhotoJpaEntity;
import com.ryuqq.otatoy.persistence.property.entity.QPropertyPhotoJpaEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PropertyPhoto 조회 전용 QueryDsl Repository.
 * 모든 조회에 deleted.isFalse() soft delete 필터를 포함한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Repository
public class PropertyPhotoQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private static final QPropertyPhotoJpaEntity propertyPhoto = QPropertyPhotoJpaEntity.propertyPhotoJpaEntity;

    public PropertyPhotoQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public List<PropertyPhotoJpaEntity> findByPropertyId(Long propertyId) {
        return queryFactory
                .selectFrom(propertyPhoto)
                .where(
                        propertyPhoto.propertyId.eq(propertyId),
                        propertyPhoto.deleted.isFalse()
                )
                .orderBy(propertyPhoto.sortOrder.asc())
                .fetch();
    }
}
