package com.ryuqq.otatoy.persistence.roomtype.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.otatoy.persistence.roomtype.entity.QRoomTypeJpaEntity;
import com.ryuqq.otatoy.persistence.roomtype.entity.RoomTypeJpaEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * RoomType 조회 전용 QueryDsl Repository.
 * 모든 조회에 deleted.isFalse() soft delete 필터를 포함한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Repository
public class RoomTypeQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private static final QRoomTypeJpaEntity roomType = QRoomTypeJpaEntity.roomTypeJpaEntity;

    public RoomTypeQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public Optional<RoomTypeJpaEntity> findById(Long id) {
        RoomTypeJpaEntity result = queryFactory
                .selectFrom(roomType)
                .where(
                        roomType.id.eq(id),
                        roomType.deleted.isFalse()
                )
                .fetchOne();
        return Optional.ofNullable(result);
    }

    public boolean existsById(Long id) {
        Integer result = queryFactory
                .selectOne()
                .from(roomType)
                .where(
                        roomType.id.eq(id),
                        roomType.deleted.isFalse()
                )
                .fetchFirst();
        return result != null;
    }

    /**
     * 특정 숙소에 속한 객실 유형 목록을 조회한다.
     */
    public List<RoomTypeJpaEntity> findByPropertyId(Long propertyId) {
        return queryFactory
                .selectFrom(roomType)
                .where(
                        roomType.propertyId.eq(propertyId),
                        roomType.deleted.isFalse()
                )
                .fetch();
    }

    /**
     * 특정 숙소의 ACTIVE 객실 유형 중 maxOccupancy >= minOccupancy 목록을 조회한다.
     * 고객 요금 조회 시 인원 필터링에 사용.
     */
    public List<RoomTypeJpaEntity> findActiveByPropertyIdAndMinOccupancy(Long propertyId, int minOccupancy) {
        return queryFactory
                .selectFrom(roomType)
                .where(
                        roomType.propertyId.eq(propertyId),
                        roomType.status.eq("ACTIVE"),
                        roomType.maxOccupancy.goe(minOccupancy),
                        roomType.deleted.isFalse()
                )
                .fetch();
    }
}
