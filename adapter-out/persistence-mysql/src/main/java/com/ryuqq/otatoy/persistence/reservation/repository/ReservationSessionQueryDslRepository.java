package com.ryuqq.otatoy.persistence.reservation.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.otatoy.persistence.reservation.entity.QReservationSessionJpaEntity;
import com.ryuqq.otatoy.persistence.reservation.entity.ReservationSessionJpaEntity;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * ReservationSession 조회 전용 QueryDsl Repository.
 * reservation_session 테이블은 soft delete 없음 — deleted 조건 불필요.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Repository
public class ReservationSessionQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private static final QReservationSessionJpaEntity session = QReservationSessionJpaEntity.reservationSessionJpaEntity;

    public ReservationSessionQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public Optional<ReservationSessionJpaEntity> findById(Long id) {
        ReservationSessionJpaEntity result = queryFactory
                .selectFrom(session)
                .where(session.id.eq(id))
                .fetchOne();
        return Optional.ofNullable(result);
    }

    /**
     * 멱등키로 세션을 조회한다.
     */
    public Optional<ReservationSessionJpaEntity> findByIdempotencyKey(String idempotencyKey) {
        ReservationSessionJpaEntity result = queryFactory
                .selectFrom(session)
                .where(session.idempotencyKey.eq(idempotencyKey))
                .fetchOne();
        return Optional.ofNullable(result);
    }

    /**
     * PENDING 상태이면서 주어진 시각 이전에 생성된 세션 목록을 조회한다.
     * 좀비 세션 복구 스케줄러에서 사용한다.
     */
    public List<ReservationSessionJpaEntity> findPendingBefore(Instant cutoff) {
        return queryFactory
                .selectFrom(session)
                .where(
                        session.status.eq("PENDING"),
                        session.createdAt.lt(cutoff)
                )
                .fetch();
    }
}
