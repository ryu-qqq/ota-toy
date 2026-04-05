package com.ryuqq.otatoy.persistence.partner.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.otatoy.persistence.partner.entity.PartnerJpaEntity;
import com.ryuqq.otatoy.persistence.partner.entity.QPartnerJpaEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Partner 조회 전용 QueryDsl Repository.
 * 모든 조회에 deleted.isFalse() soft delete 필터를 포함한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Repository
public class PartnerQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private static final QPartnerJpaEntity partner = QPartnerJpaEntity.partnerJpaEntity;

    public PartnerQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public Optional<PartnerJpaEntity> findById(Long id) {
        PartnerJpaEntity result = queryFactory
                .selectFrom(partner)
                .where(
                        partner.id.eq(id),
                        partner.deleted.isFalse()
                )
                .fetchOne();
        return Optional.ofNullable(result);
    }

    public boolean existsById(Long id) {
        Integer result = queryFactory
                .selectOne()
                .from(partner)
                .where(
                        partner.id.eq(id),
                        partner.deleted.isFalse()
                )
                .fetchFirst();
        return result != null;
    }
}
