package com.ryuqq.otatoy.application.reservation.scheduler;

import com.ryuqq.otatoy.application.common.factory.TimeProvider;
import com.ryuqq.otatoy.application.inventory.manager.InventoryClientManager;
import com.ryuqq.otatoy.application.reservation.port.out.ReservationSessionCommandPort;
import com.ryuqq.otatoy.application.reservation.port.out.ReservationSessionQueryPort;
import com.ryuqq.otatoy.domain.reservation.ReservationSession;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * 좀비 예약 세션 복구 스케줄러.
 * PENDING 상태로 10분 이상 경과한 세션의 Redis 재고를 복구하고, 세션 상태를 EXPIRED로 변경한다.
 * 30초 주기로 실행된다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class ReservationSessionRecoveryScheduler {

    private static final long SESSION_TTL_MINUTES = 10;

    private final ReservationSessionQueryPort sessionQueryPort;
    private final ReservationSessionCommandPort sessionCommandPort;
    private final InventoryClientManager inventoryClientManager;
    private final TimeProvider timeProvider;

    public ReservationSessionRecoveryScheduler(ReservationSessionQueryPort sessionQueryPort,
                                                ReservationSessionCommandPort sessionCommandPort,
                                                InventoryClientManager inventoryClientManager,
                                                TimeProvider timeProvider) {
        this.sessionQueryPort = sessionQueryPort;
        this.sessionCommandPort = sessionCommandPort;
        this.inventoryClientManager = inventoryClientManager;
        this.timeProvider = timeProvider;
    }

    /**
     * 만료된 세션의 재고를 복구한다.
     * 개별 세션 처리 실패 시 다음 주기에 재시도한다.
     */
    @Scheduled(fixedDelay = 30000)
    public void recoverExpiredSessions() {
        Instant cutoff = timeProvider.now().minus(Duration.ofMinutes(SESSION_TTL_MINUTES));
        List<ReservationSession> expiredSessions = sessionQueryPort.findPendingBefore(cutoff);

        for (ReservationSession session : expiredSessions) {
            try {
                session.expire(timeProvider.now());
                inventoryClientManager.incrementStock(session.roomTypeId(), session.stayDates());
                sessionCommandPort.persist(session);
            } catch (Exception e) {
                // 개별 실패 시 다음 주기에 재시도 — 로깅은 인프라 레이어에서 처리
            }
        }
    }
}
