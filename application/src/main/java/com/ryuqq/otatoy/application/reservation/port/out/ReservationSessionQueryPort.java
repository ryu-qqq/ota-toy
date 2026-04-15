package com.ryuqq.otatoy.application.reservation.port.out;

import com.ryuqq.otatoy.domain.reservation.ReservationSession;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * ReservationSession 조회 전용 Outbound Port.
 * Adapter(persistence-mysql)에서 구현한다 (APP-PRT-001).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface ReservationSessionQueryPort {

    /**
     * ID로 예약 세션을 조회한다.
     *
     * @param id 세션 ID
     * @return 세션 (없으면 empty)
     */
    Optional<ReservationSession> findById(Long id);

    /**
     * 멱등키로 예약 세션을 조회한다.
     *
     * @param idempotencyKey 멱등키 (UUID)
     * @return 세션 (없으면 empty)
     */
    Optional<ReservationSession> findByIdempotencyKey(String idempotencyKey);

    /**
     * 예약 ID로 예약 세션을 조회한다.
     * 예약 취소 시 roomTypeId를 추출하기 위해 사용한다.
     *
     * @param reservationId 예약 ID
     * @return 세션 (없으면 empty)
     */
    Optional<ReservationSession> findByReservationId(Long reservationId);

    /**
     * PENDING 상태이면서 주어진 시각 이전에 생성된 세션 목록을 조회한다.
     * 좀비 세션 복구 스케줄러에서 사용한다.
     *
     * @param cutoff 기준 시각 (이 시각 이전에 생성된 세션)
     * @return 만료 대상 세�� 목록
     */
    List<ReservationSession> findPendingBefore(Instant cutoff);
}
