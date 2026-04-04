package com.ryuqq.otatoy.domain.common.vo;

import java.time.Instant;

/**
 * 논리 삭제 상태를 나타내는 VO.
 * deleted=true이면 삭제된 상태, deletedAt에 삭제 시점을 기록한다.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public record DeletionStatus(boolean deleted, Instant deletedAt) {

    public static DeletionStatus active() {
        return new DeletionStatus(false, null);
    }

    public static DeletionStatus deleted(Instant deletedAt) {
        return new DeletionStatus(true, deletedAt);
    }
}
