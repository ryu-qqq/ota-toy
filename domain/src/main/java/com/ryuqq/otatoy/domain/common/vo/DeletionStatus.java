package com.ryuqq.otatoy.domain.common.vo;

import java.time.Instant;

public record DeletionStatus(boolean deleted, Instant deletedAt) {

    public static DeletionStatus active() {
        return new DeletionStatus(false, null);
    }

    public static DeletionStatus deleted(Instant deletedAt) {
        return new DeletionStatus(true, deletedAt);
    }
}
